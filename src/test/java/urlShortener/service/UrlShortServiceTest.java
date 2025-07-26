package urlShortener.service;

import org.testng.annotations.Test;
import urlShortener.model.AccessRecord;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UrlShortServiceTest {
    private UrlShortenerService urlShortService = new UrlShortenerService();
//    {
//        urlShortService.shortenUrl("http://www.google.com");
//        urlShortService.shortenUrl("http://www.microsoft.com");
//        urlShortService.shortenUrl("http://www.apple.com");
//        urlShortService.shortenUrl("http://www.facebook.com");
//        urlShortService.shortenUrl("http://www.squarespace.com");
//    }
    @Test
    public void testShorteningUrls () {
        String urlString1 = "http://www.squarespace.com";
        String urlString2 = "http://www.squarespace2.com";
        String shortUrl1 = urlShortService.shortenUrl(urlString1);
        String shortUrl2 = urlShortService.shortenUrl(urlString2);
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(1, urlShortService.accessHistory.size());
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString2, urlShortService.expandUrl(shortUrl2));
        assertEquals(urlString1, urlShortService.expandUrl(shortUrl1));

        assertEquals(9, urlShortService.accessHistory.size());

        var x = urlShortService.computeStatistics();
        for (Map.Entry<AccessRecord, Long> accessRecordLongEntry : x.entrySet()) {
            System.out.println("Record : " + accessRecordLongEntry.toString());
        }


    }

    @Test
    public void testHistory () {
        //assertTrue(urlShortService.computeStatistics().isEmpty());
        AccessRecord ac1 = new AccessRecord("abc", LocalDateTime.now());
        AccessRecord ac2 = new AccessRecord("abc", LocalDateTime.now().minusDays(1));
        assertEquals(ac1, ac2);
    }

    /*
    public static void main(String[] args) {
        UrlShortService service = new UrlShortService();

        System.out.println("=== SimpleTinyURL Data Structures Test ===");

        // Test base62 encoding
        System.out.println("Base62 encoding tests:");
        System.out.println("100000 -> " + service.encodeBase62(100000));
        System.out.println("100001 -> " + service.encodeBase62(100001));
        System.out.println("999999 -> " + service.encodeBase62(999999));

        // Test URLRecord creation
        URLRecord record = new URLRecord("abc123", "https://example.com", System.currentTimeMillis());
        System.out.println("\nURLRecord test: " + record);

        // Test data structure initialization
        System.out.println("\nData structures initialized:");
        System.out.println("shortToLong size: " + service.shortToLong.size());
        System.out.println("longToShort size: " + service.longToShort.size());
        System.out.println("Current counter: " + service.counter);
    }
    */
    //    @Test
//    public void testSavingToFile () {
//        shortToLong.put("key1", new URLRecord(
//                "key1",
//                "http://coalfire.com",
//                LocalDateTime.now().minusMinutes(12).toInstant(ZoneOffset.UTC).toEpochMilli()));
//        shortToLong.put("key2", new URLRecord(
//                "key2",
//                "http://google.com",
//                LocalDateTime.now().minusMinutes(10).toInstant(ZoneOffset.UTC).toEpochMilli()));
//        shortToLong.put("key3", new URLRecord(
//                "key3",
//                "http://apple.com",
//                LocalDateTime.now().minusMinutes(5).toInstant(ZoneOffset.UTC).toEpochMilli()));
//        longToShort.put("http://coalfire.com", "key1");
//        longToShort.put("http://google.com", "key2");
//        longToShort.put("http://apple.com", "key3");
//        saveToFile();
//        longToShort.clear();
//        shortToLong.clear();
//        assertTrue(longToShort.isEmpty());
//        loadFromFile();
//        assertFalse(shortToLong.isEmpty());
//        for ( Map.Entry<String , String> entry : longToShort.entrySet()) {
//            System.out.println(entry);
//        }
//
//
//
//    }
//
//    @Test
//    public void testSerializeRecord () {
//        URLRecord record = new URLRecord("key1", "http://fufu.com", 12345);
//        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("myrecord.ser"))) {
//            oos.writeObject(record);
//        } catch (IOException e) {
//            e.getMessage();
//        }
//    }
}
