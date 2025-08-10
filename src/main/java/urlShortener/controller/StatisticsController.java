package urlShortener.controller;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/stats")
public class StatisticsController {

    @Autowired
    private MongoTemplate mongoTemplate;

    //find 10 most used URLs.
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
                        "shortUrl", Optional.ofNullable(doc.getString("shortUrl")).orElse("short is NULL"),
                        "count", Optional.ofNullable(doc.get("count")).orElse("count is NULL"),
                        "originalUrl", Optional.ofNullable(doc.getString("originalUrl")).orElse("original is NULL")
                ))
                .toList();
    }
}
