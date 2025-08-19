package urlShortener.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "accessRecord")
public record AccessRecord(String shortURL, LocalDateTime accessTime) {
    @Override
    public boolean equals (Object object) {
        if (this == object) return true;
        if (!(object instanceof AccessRecord otherRecord)) return false;
        return this.shortURL.equals(otherRecord.shortURL);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortURL);
    }
}
