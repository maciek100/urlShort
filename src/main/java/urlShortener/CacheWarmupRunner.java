package urlShortener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import urlShortener.repository.ShortUrlRepository;
import urlShortener.service.UrlShortenerService;


//@Component
//@Order(1)
public class CacheWarmupRunner implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(CacheWarmupRunner.class.getName());
    @Autowired
    UrlShortenerService urlShortenerService;
    @Autowired
    ShortUrlRepository shortUrlRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.info("WARMING UP CACHES");

        urlShortenerService.warmupCashesFromRepository(shortUrlRepository.findAll())
                .subscribe(
                        null,
                        err -> logger.error("Cache warmup failed", err),
                        () -> logger.info("Cache warmup complete")
                );
        //TODO: Do you want me to also add a counter of warmed-up records in the logs,
        // so you can verify how many keys were pushed to Memcached during startup?
    }
}