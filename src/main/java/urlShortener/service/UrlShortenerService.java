package urlShortener.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import urlShortener.model.AccessRecord;
import urlShortener.model.URLRecord;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Simple TinyURL Service - Step by Step Implementation
 * Starting with basic data structures
 */
@Service
public class UrlShortenerService {
    private final Logger logger = Logger.getLogger(UrlShortenerService.class.getName());
    @Autowired
    private Environment environment;

    @PostConstruct
    public void retrievePreviousRunData() {
        this.loadFromFile();
    }

    // Base URL for our service
    private static final String BASE_URL = "https://tiny.ly/";


    // Core data structures
    private Map<String, URLRecord> shortToLong;    // shortCode -> URLRecord
    private Map<String, String> longToShort;      // originalUrl -> shortCode
    List<AccessRecord> accessHistory;
    // File for persistence
    @Value("${short.service.storage.file.name}")
    private String DATA_FILE;
    @Value("${keep.record.alive}")
    private long expiryTime;

    /**
     * URLRecord - stores all information about a shortened URL
     */

    public UrlShortenerService() {
        this.shortToLong = new HashMap<>();
        this.longToShort = new HashMap<>();
        this.accessHistory = new ArrayList<>();
    }

    /**
     * Load data from file (if exists)
     */
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            logger.info("No existing data file found. Starting fresh.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            this.shortToLong = (Map<String, URLRecord>) ois.readObject();
            this.longToShort = (Map<String, String>) ois.readObject();
            logger.info("Loaded " + shortToLong.size() + " URLs from file.");
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error loading data from file: " +  e.getMessage());
            this.shortToLong = new HashMap<>();
            this.longToShort = new HashMap<>();
        }
    }

    /**
     * Save data to file
     */
    public void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(shortToLong);
            oos.writeObject(longToShort);
            logger.log(Level.INFO,"Saved " + shortToLong.size() + " URLs to file.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving data to file: " + e.getMessage());
        }
    }

    public String expandUrl (String shortUrl) {
        if (shortToLong.containsKey(shortUrl)) {
            accessHistory.add(new AccessRecord(shortUrl, LocalDateTime.now()));
            return shortToLong.get(shortUrl).originalURL();
        } else
            return null;
    }

    public String shortenUrl(String realUrl) {
        if (longToShort.containsKey(realUrl))
            return longToShort.get(realUrl);
        String shortCode;
        int attempts = 0;
        do {
            shortCode = generateShortCode();
            attempts++;
        } while (shortToLong.containsKey(shortCode) && attempts < 10);
        if (attempts >= 10) {
            throw new RuntimeException("Could not generate unique short code");
        }
        shortToLong.put(shortCode,
                new URLRecord(shortCode, realUrl, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()));
        longToShort.put(realUrl, shortCode);
        return shortCode;
    }

    private static String generateShortCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0,6);
    }

    public Map<String, Long> computeStatistics () {
        return  accessHistory.stream()
                .map(AccessRecord::shortURL)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()));
    }

    public List<URLRecord> expireOldUrlRecords() {
        long now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        shortToLong.values().stream()
                .map(urlRecord -> {
                    if (urlRecord.createdAt() + expiryTime < now)
                        urlRecord.
                });
    }
}

