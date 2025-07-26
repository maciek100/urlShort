package urlShortener.model;

import java.io.Serializable;
import java.util.Objects;

public record URLRecord(String shortCode, String originalURL, long createdAt, boolean expired) implements Serializable {
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

