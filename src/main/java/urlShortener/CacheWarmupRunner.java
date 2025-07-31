package urlShortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import urlShortener.model.URLRecord;
import urlShortener.repository.ShortUrlRepository;
import urlShortener.service.UrlShortenerService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Order(1)
public class CacheWarmupRunner implements ApplicationRunner {
    Logger logger = Logger.getLogger(CacheWarmupRunner.class.getName());
    @Autowired
    UrlShortenerService urlShortenerService;
    @Autowired
    ShortUrlRepository shortUrlRepository;

    @Override
    public void run(ApplicationArguments args) {
        logger.log(Level.INFO,"WARMING UP CACHES");
        List<URLRecord> listOfRecords = shortUrlRepository.findAll();
        logger.log(Level.INFO,"Found " + listOfRecords.size() + " records to warm up cache");
        urlShortenerService.warmupCashesFromRepository(listOfRecords);
    }
}