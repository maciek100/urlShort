package urlShortener.model;

import java.time.LocalDateTime;
import java.util.Objects;

public record AccessRecord(String shortURL, LocalDateTime accessTime) {
    @Override
    public boolean equals (Object object) {
        if (this == object)
            return true;
        if (!(object instanceof AccessRecord otherRecord))
            return false;

        return this.shortURL.equals(otherRecord.shortURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortURL);
    }
}
