package urlShortener.service;

import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import urlShortener.dto.CacheStats;
import urlShortener.model.AccessRecord;
import urlShortener.model.URLRecord;
import urlShortener.repository.AccessRecordRepository;
import urlShortener.repository.ShortUrlRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Simple TinyURL Service - Step by Step Implementation
 * Starting with basic data structures
 */
@Service
public class UrlShortenerService {
    private final Logger logger = LoggerFactory.getLogger(UrlShortenerService.class.getName());
    private final ShortUrlRepository shortUrlRepository;
    private final AccessRecordRepository accessRecordRepository;
    private final MemcachedClient memcachedClient;
    private final long timeToLive;

    @Autowired
    public UrlShortenerService(
            ShortUrlRepository shortUrlRepository,
            AccessRecordRepository accessRecordRepository,
            MemcachedClient memcachedClient,
            @Value("${time.to.live.seconds}") long timeToLive) {
        this.shortUrlRepository = shortUrlRepository;
        this.accessRecordRepository = accessRecordRepository;
        this.memcachedClient = memcachedClient;
        this.timeToLive = timeToLive;
    }

    public Mono<Void> warmupCashesFromRepository(Flux<URLRecord> records) {
        return records
                .flatMap(this::warmupCache)
                .doOnNext(v -> logger.info("Record warmed up"))
                .then() // ensures we return Mono<Void>
                .doOnSuccess(v -> logger.info("All records warmed up"))
                .doOnError(err -> logger.error("Error during cache warmup", err));
    }

    private Mono<Void> warmupCache(URLRecord record) {
        return Mono.fromRunnable(() -> {
            memcachedClient.add("short:" + record.getShortCode(), 3600, record);
            memcachedClient.add("long:" + record.getOriginalURL(), 3600, record.getShortCode());
        });
    }

    public Mono<String> expandUrl (String shortUrl) {
        return getUrlRecord(shortUrl)
                .flatMap(existing -> {
                    if (existing.isExpired()) {
                        // return a "not found" style error -> 404
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.GONE, "Short URL [" + shortUrl + "] has expired"));
                    }
                    logger.info("Found Record {}", existing);
                    AccessRecord accessRecord = new AccessRecord(shortUrl, LocalDateTime.now());
                    return accessRecordRepository.save(accessRecord)
                            .thenReturn(existing.getOriginalURL());
                })
                .switchIfEmpty(Mono.fromRunnable(() ->
                        logger.warn("Short URL for {} not found in the repository.", shortUrl)
                ).then(Mono.empty()));
    }

    public Mono<String> shortenUrl(String realUrl) {
        var cacheKey = "long:" + realUrl;
        Object cachedShortURL = memcachedClient.get(cacheKey);
        if (cachedShortURL != null) {
            logger.info("FOUND CACHED SHORT url -> {} for real URL {}", cachedShortURL, realUrl);
            return Mono.just(cachedShortURL.toString());
        }
        //produce NEW short URL
        String shortCode = generateShortCodeWrapper();
        Instant creation = Instant.now();
        Instant termination = creation.plus(Duration.ofSeconds(timeToLive));
        URLRecord urlRecord = new URLRecord(shortCode, realUrl, creation, termination);
        return shortUrlRepository.save(urlRecord)
                .doOnSuccess(saved -> {
                    cacheMapping(realUrl, shortCode, urlRecord);
                    logger.info("saved url {} as {}", realUrl, shortCode);
                })
                .map(URLRecord::getShortCode);
    }

    private String generateShortCodeWrapper () {
        String shortCode;
        int attempts = 0;
        do {
            shortCode = generateShortCode();
            attempts++;
        } while (memcachedClient.get("short:" + shortCode) != null && attempts < 10);
        if (attempts >= 10) {
            throw new RuntimeException("Could not generate UNIQUE short code");
        }
        return shortCode;
    }

    private String generateShortCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0,6);
    }

    private void cacheMapping(String longUrl, String shortUrl, URLRecord record) {
        memcachedClient.set("long:" + longUrl, 3600, shortUrl);
        memcachedClient.set("short:" + shortUrl, 3600, record);
    }

    private Mono<URLRecord> getUrlRecord(String shortUrl) {
        Object record = memcachedClient.get("short:" + shortUrl);
        if (record != null) {
            URLRecord urlRecord =  (URLRecord) record;
            return Mono.just(urlRecord);
        } else {
            logger.warn("Record {} not in the cache", shortUrl);
        }
        //cache does not have the key ... let's go to repository
        return shortUrlRepository.findById(shortUrl);
        ////Mono<URLRecord> urlRecordOptional = shortUrlRepository.findById(shortUrl);
        /////return urlRecordOptional.orElse(null);
    }

    // Reactive method to get current cache size
    public Mono<CacheStats> getCacheSize() {
        return Mono.fromCallable(() -> {
                    int size = memcachedClient.getStats().values().stream()
                            .mapToInt(innerMap -> {
                                try {
                                    return Integer.parseInt(innerMap.getOrDefault("curr_items", "0"));
                                } catch (NumberFormatException ex) {
                                    return 0;
                                }
                            })
                            .sum();
                    return new CacheStats(size, 200, "ok");
                })
                .subscribeOn(Schedulers.boundedElastic()) // runs blocking call safely
                .onErrorResume(e -> Mono.just(new CacheStats(0, 500, e.getMessage())));
    }
}

