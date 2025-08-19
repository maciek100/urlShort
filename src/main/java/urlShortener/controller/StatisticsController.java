package urlShortener.controller;

import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public StatisticsController (ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    //find 10 most used URLs.
    @GetMapping("/top")
    public Flux<Map<String, Object>> getTopUrlsWithOriginals () {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("shortURL").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(100),
                Aggregation.project("count").and("_id").as("shortUrl"),
                Aggregation.lookup("urlRecord", "shortUrl", "_id", "urlInfo"),
                Aggregation.unwind("urlInfo", true),
                Aggregation.project("shortUrl", "count")
                        .and("urlInfo.originalURL").as("originalUrl")
                        .and("urlInfo.expired").as("expired")
        );
        return reactiveMongoTemplate
                .aggregate(aggregation, "accessRecord", Document.class)
                .doOnNext(doc -> System.out.println("document:\n" + doc.toJson() + "\n"))
                .map(doc -> Map.<String, Object>of(
                        "shortUrl", Optional.ofNullable(doc.getString("shortUrl")).orElse("short is NULL"),
                        "count", Optional.ofNullable(doc.get("count")).orElse("count is NULL"),
                        "originalUrl", Optional.ofNullable(doc.getString("originalUrl")).orElse("original is NULL"),
                        "expired", Optional.ofNullable(doc.get("expired", Boolean.class)).orElse(false)
                ));
    }
}
