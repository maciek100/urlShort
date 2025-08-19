package urlShortener.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import urlShortener.dto.URLAccessStats;
import urlShortener.model.URLRecord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class StatisticsService {
    Logger logger = LoggerFactory.getLogger(StatisticsService.class.getName());

    ReactiveMongoTemplate reactiveMongoTemplate;

    public StatisticsService (ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    @PostConstruct
    public void initializeTTLIndex () {
        Index ttlIndex = new Index()
                .on("expiresAt", Sort.Direction.ASC)
                .expire(Duration.ofSeconds(0));

        reactiveMongoTemplate.indexOps("urlRecord")
                .createIndex(ttlIndex)
                .doOnSuccess(i -> logger.info("TTL index ensured"))
                .subscribe();
    }

    private Flux<URLAccessStats> computeStatistics (LocalDateTime from, LocalDateTime to) {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.match(Criteria.where("accessTime").gte(from).lte(to)),
            Aggregation.group("shortURL")
                    .count().as("total")
                    .min("accessTime").as("firstAccess")
                    .max("accessTime").as("lastAccess")
        );

        return reactiveMongoTemplate.aggregate(aggregation, "accessRecord", URLAccessStats.class)
                .doOnNext(stats -> logger.info("STAT: {}", stats));
    }

    public Flux<URLAccessStats> computeStatisticsForDefinedTimePeriod (Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        return computeStatistics(now.minusHours(duration.toHours()), now);
    }

    public Flux<Map<String, URLAccessStats>> computeStatisticsMapForPeriod(Duration duration) {
        return computeStatisticsForDefinedTimePeriod(duration)
                .collectMap(URLAccessStats::_id, Function.identity())
                .flux(); // wrap Map in a Flux
    }

    /**
     * Top N URLs by usage (reactive version).
     */
    public Flux<Map<String, Object>> getTopUrlsWithOriginals(int limit) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("shortURL").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(limit),
                Aggregation.project("count").and("_id").as("shortUrl"),
                Aggregation.lookup("urlRecord", "shortUrl", "_id", "urlInfo"),
                Aggregation.unwind("urlInfo", true),
                Aggregation.project("shortUrl", "count")
                        .and("urlInfo.originalURL").as("originalUrl")
        );

        return reactiveMongoTemplate.aggregate(aggregation, "accessRecord", Map.class)
                .map(doc -> Map.of(
                        "shortUrl", doc.getOrDefault("shortUrl", "short is NULL"),
                        "count", doc.getOrDefault("count", "count is NULL"),
                        "originalUrl", doc.getOrDefault("originalUrl", "original is NULL")
                ));
    }

    public Mono<Map<String, Object>> expireRandomUrlRecords(int count) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.sample(count) // pick 'count' random documents
        );
                return reactiveMongoTemplate.aggregate(aggregation, "urlRecord", URLRecord.class)
                .collectList()
                .flatMap(urlRecords -> {
                    List<String> pickedIds = urlRecords.stream()
                            .map(URLRecord::getShortCode) // assuming getId() returns shortCode
                            .toList();

                    long alreadyExpired = urlRecords.stream()
                            .filter(URLRecord::isExpired)
                            .count();

                    List<URLRecord> toExpire = urlRecords.stream()
                            .filter(r -> !r.isExpired())
                            .peek(r -> r.setExpired(true))
                            .toList();

                    return Flux.fromIterable(toExpire)
                            .flatMap(reactiveMongoTemplate::save)
                            .then(Mono.just(Map.of(
                                    "pickedIds", pickedIds,
                                    "alreadyExpired", alreadyExpired,
                                    "newlyExpired", toExpire.size()
                            )));
                });
    }


}
