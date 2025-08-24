package urlShortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import urlShortener.model.URLRecord;

import java.time.Duration;
import java.util.Optional;
import net.spy.memcached.MemcachedClient;

@Service
@Profile("memcached")
public class MemcachedUrlCache implements UrlCache {
    private final Logger logger = LoggerFactory.getLogger(MemcachedUrlCache.class.getName());
    private final MemcachedClient memcachedClient;
    private final Duration defaultTtl;

    public MemcachedUrlCache(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
        this.defaultTtl = Duration.ofHours(1);
    }

    @Override
    public Optional<String> getShortForReal(String realUrl) {
        var cacheKey = "long:" + realUrl;
        Object cachedShortURL = memcachedClient.get(cacheKey);
        if (cachedShortURL != null) {
            logger.info("FOUND MEMCACHED SHORT url -> {} for real URL {}", cachedShortURL, realUrl);
            return Optional.ofNullable(cachedShortURL.toString());
        }

        return Optional.empty();
    }

    @Override
    public Optional<URLRecord> getRecordByShort(String shortCode) {
        return Optional.empty();
    }

    private static String kLong(String real) {
        return "long:" + real;
    }

    private static String kShort(String code) {
        return "short:" + code;
    }

    @Override
    public void putMappings(URLRecord record, Duration ttl) {
        Duration t = (ttl != null ? ttl : defaultTtl);
        memcachedClient.set(kLong(record.getOriginalURL()), 3600, record.getShortCode());
        memcachedClient.set(kShort(record.getShortCode()), 3600, record);
    }

    @Override
    public long size() {
        logger.info("ASKED ABOUT MEMCACHED CACHE SIZE");
        try {
            return memcachedClient.getStats().values().stream()
                    .mapToInt(innerMap -> {
                        try {
                            int z =  Integer.parseInt(innerMap.getOrDefault("curr_items", "-3"));
                            logger.info("DISCOVERED MEMCACHED SIZE TO BE = {}", z);
                            return z;
                        } catch (NumberFormatException ex) {
                            logger.error("NFE {}", ex.getMessage());
                            return 0;
                        }
                    })
                    .sum();
        } catch (Exception e) {
            logger.error("OTHER EXCEPTION {}", e.getMessage());
            return 0L; // fallback on error
        }
    }
}
