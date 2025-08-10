package urlShortener.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Document("urlRecord")
public record URLRecord(
        @Id String shortCode,
        String originalURL,
        Instant createdAt,
        Instant expiresAt) implements Serializable {

    public boolean equals(Object otherObject) {
        boolean result = false;
        if (otherObject != null) {
            if (otherObject instanceof URLRecord otherRecord) {
                result = this.shortCode.equals(otherRecord.shortCode) && (this.originalURL.equals(otherRecord.originalURL));
            }
        }
        return result;
    }

    public int hashCode () {
        return Objects.hash(shortCode, originalURL);
    }
};

