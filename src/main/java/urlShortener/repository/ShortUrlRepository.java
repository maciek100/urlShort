package urlShortener.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import urlShortener.model.URLRecord;

import java.util.Optional;

public interface ShortUrlRepository extends MongoRepository<URLRecord, String> {
    Optional<URLRecord> findByShortCode (String shortCode);
}
