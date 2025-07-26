package urlShortener.model;

import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.*;

public class URLRecordTest {

    @Test
    public void recordTest () {
        URLRecord record1 = new URLRecord("abc"," http://google.com", 1L);
        URLRecord record2 = new URLRecord("abc"," http://google.com", 2L);
        assertEquals(record1, record2);
        assertEquals(record1.hashCode(), record2.hashCode());
    }
}
