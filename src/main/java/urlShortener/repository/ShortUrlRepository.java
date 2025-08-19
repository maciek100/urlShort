package urlShortener.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import urlShortener.model.URLRecord;

import java.time.Instant;

@Repository
public interface ShortUrlRepository extends ReactiveMongoRepository<URLRecord, String> {
    Mono<URLRecord> findByShortCode (String shortCode);
    Flux<URLRecord> findByExpiresAtBeforeAndExpiredFalse(Instant now);
}
