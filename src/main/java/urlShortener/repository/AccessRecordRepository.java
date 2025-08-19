package urlShortener.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import urlShortener.model.AccessRecord;

import java.time.LocalDateTime;

@Repository
public interface AccessRecordRepository extends ReactiveMongoRepository<AccessRecord, String> {
    Flux<AccessRecord> findByAccessTimeBetween(LocalDateTime from, LocalDateTime to);
}
