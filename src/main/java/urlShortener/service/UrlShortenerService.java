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
import java.util.Optional;
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
    private final UrlCache urlCache;
    private final Duration timeToLive;

    @Autowired
    public UrlShortenerService(
            ShortUrlRepository shortUrlRepository,
            AccessRecordRepository accessRecordRepository,
            UrlCache urlCache,
            @Value("${time.to.live.seconds}") long timeToLive) {
        this.shortUrlRepository = shortUrlRepository;
        this.accessRecordRepository = accessRecordRepository;
        this.urlCache = urlCache;
        this.timeToLive = Duration.ofSeconds(timeToLive);
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
        logger.info("THE WARM UP IS CURRENTLY NOT IMPLEMENTED.");
        //return Mono.fromRunnable(() -> {
        //    memcachedClient.add("short:" + record.getShortCode(), 3600, record);
        //    memcachedClient.add("long:" + record.getOriginalURL(), 3600, record.getShortCode());
        //});
        return Mono.empty(); //TODO: we are not warming up the cache ... correct it!
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
        //check cache real -> short
        logger.info("ATTEMPT to shorten url: {}", realUrl);
        var cached = urlCache.getShortForReal(realUrl);
        if (cached.isPresent()) {
            logger.info("FOUND in CACHE {} -> {}", realUrl, cached.get());
            return Mono.just(cached.get());
        }
        return generateUniqueShortCode()
                .flatMap(shortCode -> {
                    String shortKey = "short:" + shortCode;
                    Instant now = Instant.now();
                    URLRecord record = new URLRecord(shortKey, realUrl, now, now.plus(timeToLive));
                    // Save to DB
                    return shortUrlRepository.save(record)
                            .doOnSuccess(saved -> logger.info("Saved to Mongo: {}", saved))
                            .then(Mono.fromCallable(() -> {
                                // Save to cache
                                urlCache.putMappings(record, Duration.ofMinutes(30));
                                logger.info("Saved to Cache: {} -> {}", realUrl, shortCode);
                                return shortCode;
                            }));
                });
    }

    private Mono<String> generateUniqueShortCode () {
        String shortCode;
        int attempts = 0;
        do {
            shortCode = generateShortCode();
            attempts++;
        } while (shortUrlRepository.findById("short:" + shortCode).block() != null && attempts < 10);

        if (attempts >= 10) {
            throw new RuntimeException("Could not generate UNIQUE short code");
        }
        return Mono.just(shortCode);
    }

    private String generateShortCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0,6);
    }

    private Mono<URLRecord> getUrlRecord(String shortUrl) {
        return Mono.defer(() -> {
            // 1. Try cache first
            Optional<URLRecord> cached = urlCache.getRecordByShort(shortUrl);
            if (cached.isPresent()) {
                logger.debug("Cache hit for {}", shortUrl);
                return Mono.just(cached.get());
            }

            // 2. Cache miss, go to repository
            logger.warn("Cache miss for {}", shortUrl);
            return shortUrlRepository.findById(shortUrl)
                    .flatMap(record -> {
                        // Put into cache with TTL (you decide duration)
                        urlCache.putMappings(record, Duration.ofHours(1));
                        return Mono.just(record);
                    });
        });

    }

    // Reactive method to get current cache size
    public Mono<Long> getCacheSize() {
        logger.info("->>>> CALLED CACHE SIZE");
        return Mono.fromCallable(urlCache::size)
                .subscribeOn(Schedulers.boundedElastic());
    }
}

