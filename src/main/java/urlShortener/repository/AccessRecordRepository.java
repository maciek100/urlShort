package urlShortener.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import urlShortener.model.AccessRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessRecordRepository extends MongoRepository<AccessRecord, String> {
    List<AccessRecord> findByAccessTimeBetween(LocalDateTime from, LocalDateTime to);
}
