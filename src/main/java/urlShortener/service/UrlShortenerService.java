package urlShortener.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import urlShortener.model.AccessRecord;
import urlShortener.model.URLRecord;
import urlShortener.repository.AccessRecordRepository;
import urlShortener.repository.ShortUrlRepository;

import java.io.*;
//import java.nio.file.Path;
//import java.nio.file.Paths;
import java.rmi.AccessException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    @Autowired
    ShortUrlRepository shortUrlRepository;
    @Autowired
    AccessRecordRepository accessRecordRepository;

    @PostConstruct
    public void retrievePreviousRunData() {
        //this.loadFromFile();
    }

    // Base URL for our service
    private static final String BASE_URL = "https://petite.ly/";


    // Core data structures
    private Map<String, URLRecord> shortToLong;    // shortCode -> URLRecord
    private Map<String, String> longToShort;      // originalUrl -> shortCode
    //List<AccessRecord> accessHistory;
    // File for persistence
    //@Value("${short.service.storage.file.name}")
    //private String DATA_FILE;
    @Value("${keep.record.alive}")
    private long lifeLimit;

    /**
     * URLRecord - stores all information about a shortened URL
     */

    public UrlShortenerService() {
        this.shortToLong = new HashMap<>();
        this.longToShort = new HashMap<>();
        //this.accessHistory = new ArrayList<>();
    }


    public void warmupCashesFromRepository(List<URLRecord> listOfRecords) {
        if (listOfRecords.isEmpty()) {
            logger.log(Level.INFO, "No previous records detected. Nothing to warm up.");
        }
        listOfRecords
                .forEach(record -> {
                    shortToLong.put(record.shortCode(), record);
                    longToShort.put(record.originalURL(), record.shortCode());
                    logger.log(Level.INFO, record.toString());
                });
    }


    /**
     * Load data from file (if exists)

    private void loadFromFile() {
        Path path = Paths.get("/urlShort/data","backup_file");
        File file = path.toFile();
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
    */

    /**
     * Save data to file

    public void saveToFile() {
        logger.log(Level.INFO, "Attempting to save URLRecords to a file " + DATA_FILE);
        Path path = Paths.get("/urlShort/data","backup_file");
        File file = path.toFile();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(shortToLong);
            oos.writeObject(longToShort);
            logger.log(Level.INFO,"Saved " + shortToLong.size() + " URLs to file.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving data to file: " + e.getMessage());
        }
        File f = new File(DATA_FILE);
        logger.log(Level.INFO,"Created File " + f.getAbsolutePath() + " of size " + f.length());
    }
*/
    public String expandUrl (String shortUrl) {
        if (shortToLong.containsKey(shortUrl)) {
            AccessRecord accessRecord = new AccessRecord(shortUrl, LocalDateTime.now());
            //accessHistory.add(accessRecord);
            accessRecordRepository.save(accessRecord);
            Optional<URLRecord> optRecord = shortUrlRepository.findById(shortUrl);
            if (optRecord.isPresent())
                return optRecord.get().originalURL();
            else {
                logger.log(Level.WARNING, "URL for " + shortUrl + " not found in the repository.");
            }
        }
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
        URLRecord urlRecord = new URLRecord(shortCode, realUrl, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
        shortUrlRepository.save(urlRecord);
        shortToLong.put(shortCode, urlRecord);
        longToShort.put(realUrl, shortCode);
        logger.log(Level.INFO, "saved url " + realUrl + " as " + shortCode);
        return shortCode;
    }

    private static String generateShortCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0,6);
    }
/*
    public Map<String, Long> computeStatistics () {
         return  accessHistory.stream()
                .map(AccessRecord::shortURL)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()));
    }
*/
    public int expireOldUrlRecords() {
        long now = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli();
        AtomicInteger removedCount = new AtomicInteger();
        shortToLong.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().createdAt() + lifeLimit < now;
            if (expired)
                removedCount.incrementAndGet();
            return expired;
            });
        return removedCount.get();
    }

    public int getCacheSize () {
        return shortToLong.size();
    }
}

