package urlShortener.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "accessRecord")
public record AccessRecord(String shortURL, LocalDateTime accessTime) {
    @Override
    public boolean equals (Object object) {
        boolean result;
        if (this == object) {
            result = true;
        } else if (!(object instanceof AccessRecord otherRecord)) {
            result = false;
        } else {
            result = this.shortURL.equals(otherRecord.shortURL);
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortURL);
    }
}
