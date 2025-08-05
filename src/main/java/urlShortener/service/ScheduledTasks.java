package urlShortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import urlShortener.dto.URLAccessStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class ScheduledTasks {

    @Autowired
    StatisticsService statisticsService;

    private static final Logger logger = Logger.getLogger(ScheduledTasks.class.getName());

    @Scheduled(cron = "0 0/2 * * * *")
    public void runStatisticsMaintenance() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        String formattedDateTime = dateTime.format(formatter).toLowerCase();
        logger.info("Computing rudimentary statistics: " +  dateTime);
        logger.info("STATISTICS at " + formattedDateTime);
        Map<String, URLAccessStats> results =  statisticsService.computeStatisticsForDefinedTimePeriod(Duration.ofDays(100000L));
        results.forEach((key, value) -> System.out.println(value.toString()));
    }

    //TODO: a scheduled task retire unused Records from the database.
}
