package urlShortener.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Document("urlRecord")
@Getter
@Setter
@ToString
public class URLRecord implements Serializable {
    @Id
    private final String shortCode;
    private final String originalURL;
    private final Instant createdAt;
    private Instant expiresAt;
    private boolean expired;

    public URLRecord (String shortCode,
                      String originalURL,
                      Instant createdAt,
                      Instant expiresAt) {
        this.shortCode = shortCode;
        this.originalURL = originalURL;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expired = false;
    }

    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (!(otherObject instanceof URLRecord otherRecord)) return false;
        return this.shortCode.equals(otherRecord.shortCode)
                && (this.originalURL.equals(otherRecord.originalURL))
                && (this.expired == otherRecord.expired);
    }

    public int hashCode () {
        return Objects.hash(shortCode, originalURL, expired);
    }

    public void markExpired () {
        this.expired = true;
    }

}

