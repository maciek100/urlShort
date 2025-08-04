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
/*
    @GetMapping("/top")
    public List<TopUrlStats> getTopUrls() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group("shortURL")
                        .count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(10)
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "accessRecord", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> new TopUrlStats(doc.getString("_id"), doc.getInteger("count")))
                .toList();
    }
 */

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
        /*
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("shortURL")
                        .count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),
                Aggregation.limit(10),
                Aggregation.lookup("urlRecord", "_id", "_id", "urlInfo"), // _id == shortURL
                Aggregation.unwind("urlInfo", true),
                Aggregation.project()
                        .and("_id").as("shortUrl")
                        .and("count").as("count")
                        .and("urlInfo.originalURL").as("originalUrl")
        );
        */
        Aggregation aggregation = Aggregation.newAggregation(
                // 1. Group by shortURL from accessRecord
                Aggregation.group("shortURL")
                        .count().as("count"),

                // 2. Sort by usage count
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count")),

                // 3. Limit to top 10
                Aggregation.limit(10),

                // 4. Rename _id to shortUrl so we can use it in $lookup
                Aggregation.project("count")
                        .and("_id").as("shortUrl"),

                // 5. Lookup originalURL from urlRecord (where _id = shortUrl)
                Aggregation.lookup("urlRecord", "shortUrl", "_id", "urlInfo"),

                // 6. Unwind the result (if it exists)
                Aggregation.unwind("urlInfo", true),

                // 7. Project the final fields
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
