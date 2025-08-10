package urlShortener.service;

import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import urlShortener.model.AccessRecord;
import urlShortener.model.URLRecord;
import urlShortener.repository.AccessRecordRepository;
import urlShortener.repository.ShortUrlRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Simple TinyURL Service - Step by Step Implementation
 * Starting with basic data structures
 */
@Service
public class UrlShortenerService {
    private final Logger logger = Logger.getLogger(UrlShortenerService.class.getName());
    //@Autowired
    //private Environment environment;
    //@Autowired
    ShortUrlRepository shortUrlRepository;
    //@Autowired
    AccessRecordRepository accessRecordRepository;
    //@Autowired
    private MemcachedClient memcachedClient;

    @Value("${time.to.live.seconds}")
    long timeToLive;

    @Autowired
    public UrlShortenerService(
            ShortUrlRepository shortUrlRepository,
            AccessRecordRepository accessRecordRepository,
            MemcachedClient memcachedClient
            //long timeToLive
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.accessRecordRepository = accessRecordRepository;
        this.memcachedClient = memcachedClient;
    }
    public void warmupCashesFromRepository(List<URLRecord> listOfRecords) {
        if (listOfRecords.isEmpty()) {
            logger.log(Level.INFO, "No previous records detected. Nothing to warm up.");
        }
        listOfRecords
                .forEach(record -> {
                    memcachedClient.add("short:" + record.shortCode(), 3600, record);
                    memcachedClient.add("long:" + record.originalURL(), 3600, record.shortCode());
                    logger.log(Level.INFO, record.toString());
                });
    }



    public String expandUrl (String shortUrl) {
        URLRecord existing = getUrlRecord(shortUrl);
        if (existing == null) {
            logger.log(Level.WARNING, "URL for " + shortUrl + " not found in the repository.");
            return null;
        }
        AccessRecord accessRecord = new AccessRecord(shortUrl, LocalDateTime.now());
        accessRecordRepository.save(accessRecord);
        return existing.originalURL();
    }

//    public String getShortCodeFromLongURL(String longUrl) {
//        String cacheKey = "long:" + longUrl;
//        Object shortUrlObj = memcachedClient.get(cacheKey);
//        return shortUrlObj != null ? shortUrlObj.toString() : null;
//    }

    public String shortenUrl(String realUrl) {
        var cacheKey = "long:" + realUrl;
        Object cachedShortURL = memcachedClient.get(cacheKey);
        if (cachedShortURL != null) {
            logger.log(Level.INFO, "FOUND CACHED SHORT url -> " + cachedShortURL + " for real URL " + realUrl);
            return cachedShortURL.toString();
        }
        //produce NEW short URL
        String shortCode = generateShortCodeWrapper();
        Instant creation = Instant.now();
        Instant termination = creation.plus(Duration.ofSeconds(timeToLive));
        URLRecord urlRecord = new URLRecord(shortCode, realUrl, creation, termination);
        shortUrlRepository.save(urlRecord);
        cacheMapping(realUrl, shortCode, urlRecord);
        logger.log(Level.INFO, "saved url " + realUrl + " as " + shortCode);
        return shortCode;
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

    private URLRecord getUrlRecord(String shortUrl) {
        Object record = memcachedClient.get("short:" + shortUrl);
        if (record != null)
            return (URLRecord) record;
        //cache does not have the key ... let's go to repository
        Optional<URLRecord> urlRecordOptional = shortUrlRepository.findById(shortUrl);
        return urlRecordOptional.orElse(null);
    }
}

