package urlShortener.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import urlShortener.dto.URLAccessStats;
import urlShortener.model.AccessRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    Logger logger = Logger.getLogger(StatisticsService.class.getName());
    @Autowired
    MongoTemplate mongoTemplate;

    private Map<String, URLAccessStats> computeStatistics (LocalDateTime from, LocalDateTime to) {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("accessTime").gte(from).lte(to)),
            Aggregation.group("shortURL")
                    .count().as("total")
                    .min("accessTime").as("firstAccess")
                    .max("accessTime").as("lastAccess")
        );

        AggregationResults<URLAccessStats> results = mongoTemplate.aggregate(
                aggregation,
                "accessRecord",
                URLAccessStats.class);
        logger.log(Level.INFO, "RETRIEVED STATISTICS " + results.getMappedResults().size());
        return results.getMappedResults()
                .stream()
                .collect(Collectors.toMap(
                        URLAccessStats::_id,
                        Function.identity()
                ));
    }

    public Map<String, URLAccessStats> computeStatisticsForDefinedTimePeriod (Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        return computeStatistics(now.minusHours(duration.toHours()), now);
    }

}
