package urlShortener.service;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import urlShortener.model.URLRecord;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Service
@Profile("redis")
public class RedisUrlCache implements UrlCache {
    private final RedisTemplate<String, Object> redis;
    private final ValueOperations<String, Object> vops;
    private final Duration defaultTtl;

    public RedisUrlCache(RedisTemplate<String, Object> redis) {
        this.redis = redis;
        this.vops = redis.opsForValue();
        this.defaultTtl = Duration.ofHours(1); // set what you want
    }

    private static String kLong(String real)  { return "long:"  + real; }
    private static String kShort(String code) { return "short:" + code; }

    @Override
    public Optional<String> getShortForReal(String realUrl) {
        Object val = vops.get(kLong(realUrl));
        return Optional.ofNullable((String) val);
    }

    @Override
    public Optional<URLRecord> getRecordByShort(String shortCode) {
        Object val = vops.get(kShort(shortCode));
        return Optional.ofNullable((URLRecord) val);
    }

    @Override
    public void putMappings(URLRecord record, Duration ttl) {
        Duration t = (ttl != null ? ttl : defaultTtl);
        vops.set(kLong(record.getOriginalURL()), record.getShortCode(), t);
        vops.set(kShort(record.getShortCode()), record, t);
    }

    @Override
    public long size() {
        return redis.execute((RedisCallback<Long>) connection -> {
            long count = 0;
            try (Cursor<byte[]> cursor =
                         connection.keyCommands().scan(
                                 ScanOptions.scanOptions().match("short:*").count(1000).build()
                         )) {
                while (cursor.hasNext()) {
                    cursor.next();
                    count++;
                }
            }
            return count;
        });
    }
}
