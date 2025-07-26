package urlShortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

@Component
public class ScheduledTasks {
    @Autowired
    UrlShortenerService urlShortService;

    private static final Logger logger = Logger.getLogger(ScheduledTasks.class.getName());

    @Scheduled(cron = "0 0/2 * * * *")
    public void runHourlyTask() {
        logger.info("Computing statistics: " +  LocalDateTime.now());
        performHourlyMaintenance();
    }

    @Scheduled(cron = "0 0/10 * * * *")
    public void runSavingToFileTask() {
        logger.info("Saving to file: " +  LocalDateTime.now());
        performSaveToFileOperation();
    }

    // Alternative: Run every hour using fixedRate
    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hour
    public void runHourlyTaskAlternative() {
        logger.info("Alternative hourly task executed");
    }

    private void performHourlyMaintenance() {
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        String formatted = time.format(formatter).toLowerCase();

        logger.info("STATISTICS at " + formatted);
        urlShortService.computeStatistics().entrySet()
                .forEach(entry -> logger.info(entry.toString()));
    }

    private void performSaveToFileOperation() {
        urlShortService.saveToFile();
    }
}
