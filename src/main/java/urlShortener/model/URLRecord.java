package urlShortener.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

@Document(collection = "urlRecord")
public record URLRecord(@Id String shortCode, String originalURL, long createdAt) implements Serializable {
    public boolean equals(Object otherObject) {
        if (otherObject == null)
            return false;
        if (otherObject instanceof URLRecord otherRecord) {
            return this.shortCode.equals(otherRecord.shortCode) && (this.originalURL.equals(otherRecord.originalURL));
        }
        return false;
    }

    public int hashCode () {
        return Objects.hash(shortCode, originalURL);
    }
};

