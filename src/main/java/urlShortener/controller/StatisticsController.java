package urlShortener.controller;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import urlShortener.dto.TopUrlStats;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/topZZZ")
    public ResponseEntity<?> debugTopAggregation() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("shortURL").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(10),
                Aggregation.project("count").and("_id").as("shortUrl"),
                Aggregation.lookup("urlRecord", "shortUrl", "_id", "urlInfo"),
                Aggregation.unwind("urlInfo", true),
                Aggregation.project("shortUrl", "count").and("urlInfo.originalURL").as("originalUrl")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "accessRecord", Document.class);

        results.getMappedResults().forEach(doc -> System.out.println("AGG REG OUTPUT: " + doc.toJson()));

        return ResponseEntity.ok(results.getMappedResults());
    }


    @GetMapping("/top")
    public List<Map<String, Object>> getTopUrlsWithOriginals () {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("shortURL")
                        .count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(10),
                Aggregation.project("count")
                        .and("_id").as("shortUrl"),
                Aggregation.lookup("urlRecord", "shortUrl", "_id", "urlInfo"),
                Aggregation.unwind("urlInfo", true),
                Aggregation.project("shortUrl", "count")
                        .and("urlInfo.originalURL").as("originalUrl")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "accessRecord", Document.class);
        return results.getMappedResults().stream()
                .map(doc -> Map.of(
                        "shortUrl", doc.getString("shortUrl"),
                        "count", doc.get("count"),
                        "originalUrl", doc.getString("originalUrl")
                ))
                .toList();
    }
}
