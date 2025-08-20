package urlShortener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import urlShortener.repository.ShortUrlRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.Locale;

@Component
public class ScheduledTasks {

    private final StatisticsService statisticsService;
    private final ShortUrlRepository shortUrlRepository;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class.getName());

    public ScheduledTasks (StatisticsService statisticsService,
                           ShortUrlRepository shortUrlRepository) {
        this.statisticsService = statisticsService;
        this.shortUrlRepository = shortUrlRepository;
    }

    //@Scheduled(cron = "0 0/12 * * * *")
    public void runStatisticsMaintenance() {
        logger.info("HF statistics maintenance should be OFF");
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        String formattedDateTime = dateTime.format(formatter).toLowerCase();
        logger.info("Computing rudimentary statistics: {}", dateTime);
        logger.info("Statistics at {}", formattedDateTime);
        statisticsService.computeStatisticsMapForPeriod(Duration.ofHours(1L))
                .doOnNext(stats -> {
                    logger.info("Computed stats for last hour: {}", stats.size());

                })
                .subscribe();
    }

    //@Scheduled(fixedRate = 60000000) // every 60s
    public void markExpiredRecords() {
        logger.warn("Marking expired records should be OFF");
        Instant now = Instant.now();
        shortUrlRepository.findByExpiresAtBeforeAndExpiredFalse(now)
                .flatMap(record -> {
                    record.setExpired(true);
                    return shortUrlRepository.save(record);
                })
                .subscribe();
    }

    // runs every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void expireRandomUrls() {
        logger.info("Expire random urls is OK");
        statisticsService.expireRandomUrlRecords(10)
                .doOnTerminate(() -> logger.info("Finished expiring records"))
                .subscribe(
                        summary -> {
                            System.out.println("===== ELIMINATION ROUND RESULTS =====");
                            System.out.println("Picked: " + summary.get("pickedIds"));
                            System.out.println("Already expired: " + summary.get("alreadyExpired"));
                            System.out.println("Newly expired: " + summary.get("newlyExpired"));
                            System.out.println("============= END ================");
                        },
                        error -> logger.warn("Error expiring URLs: {}", error.getMessage())
                );
    }


}
