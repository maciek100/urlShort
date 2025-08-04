package urlShortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Logger;

@Component
public class ScheduledTasks {

    @Autowired
    UrlShortenerService urlShortService;
    @Autowired
    StatisticsService statisticsService;

    private static final Logger logger = Logger.getLogger(ScheduledTasks.class.getName());

    @Scheduled(cron = "0 0/2 * * * *")
    public void runStatisticsMaintenance() {
        logger.info("Computing rudimentary statistics: " +  LocalDateTime.now());
        statisticsService.computeStatistics(LocalDateTime.now().minusDays(4), LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        String formatted = time.format(formatter).toLowerCase();

        logger.info("STATISTICS at " + formatted);
        urlShortService.computeStatistics().entrySet()
                .forEach(entry -> logger.info(entry.toString()));
    }

    //This task removes URLRecords which were created "before" the expiration time.
    // It does not take under the account the last time the Record was used.
    //TODO: it is probably not an optimal strategy, RETHINK IT. (or have AI to do it :) )
    //@Scheduled(cron = "0 0/3 * * * *")
    public void purgeExpiredURLRecords() {
        int expiredEntries = urlShortService.expireOldUrlRecords();
        logger.info("Purged " +  expiredEntries + "  expired URLRecords.");
    }

    // Alternative: Run every hour using fixedRate
    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hour
    public void runHourlyTaskAlternative() {
        statisticsService.computeStatisticsForLastHour();
    }

}
