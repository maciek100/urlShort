package urlShortener.service;

import urlShortener.model.URLRecord;

import java.time.Duration;
import java.util.Optional;

// domain-side port
public interface UrlCache {
    Optional<String> getShortForReal(String realUrl);
    Optional<URLRecord> getRecordByShort(String shortCode);
    void putMappings(URLRecord record, Duration ttl);
    long size(); // optional
}
