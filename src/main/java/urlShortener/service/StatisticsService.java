package urlShortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import urlShortener.dto.URLAccessStats;
import urlShortener.model.AccessRecord;
import urlShortener.repository.AccessRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class StatisticsService {
    Logger logger = Logger.getLogger(StatisticsService.class.getName());
    @Autowired
    AccessRecordRepository accessRecordRepository;
    @Autowired
    MongoTemplate mongoTemplate;

    public void computeStatistics (LocalDateTime from, LocalDateTime to) {
        LocalDateTime fromX = LocalDateTime.of(2025, 7, 1, 0, 0);
        LocalDateTime toX = LocalDateTime.of(2025, 7, 30, 23, 59, 59);

        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("accessTime").gte(fromX).lte(toX)),
            Aggregation.group("shortURL")
                    .count().as("total")
                    .min("accessTime").as("firstAccess")
                    .max("accessTime").as("lastAccess")
        );

        AggregationResults<URLAccessStats> results = mongoTemplate.aggregate(
                aggregation,
                "accessRecord",
                URLAccessStats.class);
        var data = results.getMappedResults();
        logger.log(Level.INFO, "RETrieved " + data.size());
        results.getMappedResults()
                .forEach(element -> logger.log(Level.INFO, element.toString()));

        //List<AccessRecord> recordTimeSlice = accessRecordRepository.findByAccessTimeBetween(from, to);
        //logger.log(Level.INFO, "Between " + from + " and " + to + " " + recordTimeSlice.size());
    }

    public void computeStatisticsForLastHour () {
        LocalDateTime now = LocalDateTime.now();
        computeStatistics(now.minusHours(1), now);
    }

}
