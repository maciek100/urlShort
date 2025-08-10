package urlShortener.model;

import org.testng.annotations.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class URLRecordTest {

    @Test
    public void recordTest () {
        Instant created1 = Instant.now().minus(Duration.ofMinutes(5));
        Instant created2 = Instant.now().minus(Duration.ofMinutes(6));
        Instant expires1 = Instant.now().minus(Duration.ofMinutes(2));
        Instant expires2 = Instant.now().minus(Duration.ofMinutes(3));
        URLRecord record1 = new URLRecord("abc"," http://google.com", created1, expires1);
        URLRecord record2 = new URLRecord("abc"," http://google.com", created2, expires2);
        URLRecord record3 = new URLRecord("abc"," http://googlish.com", created2, expires1);
        assertEquals(record1, record2);
        assertNotEquals(record1, record3);
        assertEquals(record1.hashCode(), record2.hashCode());
    }
}
