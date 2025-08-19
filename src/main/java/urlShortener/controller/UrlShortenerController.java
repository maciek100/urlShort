package urlShortener.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.InvalidUrlException;
import reactor.core.publisher.Mono;
import urlShortener.dto.CacheStats;
import urlShortener.dto.ShortenRequest;
import urlShortener.dto.ShortenResponse;
import urlShortener.exception.ShortenerException;
import urlShortener.service.UrlShortenerService;

@RestController
@RequestMapping("/api/v1")
public class UrlShortenerController {
    private final Logger logger = LoggerFactory.getLogger(UrlShortenerController.class.getName());
    @Autowired
    private UrlShortenerService urlShortenerService;

    @GetMapping("/cache_size")
    public Mono<ResponseEntity<CacheStats>> getCacheSize() {
        return urlShortenerService.getCacheSize()
                .map(stats -> ResponseEntity.status(stats.httpResult()).body(stats));
    }

    @GetMapping("/alive")
    public ResponseEntity<String> checkAlive () {
        return ResponseEntity.ok("Long Live the Queen!");
    }

    @PostMapping("/shorten")
    public Mono<ResponseEntity<ShortenResponse>> shortenLongUrl(@Valid @RequestBody ShortenRequest request) {
        return urlShortenerService.shortenUrl(request.getUrl())
                .map(shortUrl -> {
                    logger.info("Received request to shorten: {} -> {}", request.getUrl(), shortUrl);
                    return ResponseEntity.ok(new ShortenResponse(shortUrl, "success", null));
                })
                .onErrorResume(InvalidUrlException.class, e -> {
                    logger.error("Exception (Invalid URL) in Controller: {}",e.getMessage());
                    return Mono.just(
                            ResponseEntity.badRequest()
                                    .body(new ShortenResponse(null, "error", e.getMessage()))
                    );
                })
                .onErrorResume(Exception.class, e -> {
                    logger.error("Exception (Generic Exception) in Controller: {}", e.getMessage());
                    return Mono.just(
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(new ShortenResponse(null, "error", "Internal server error"))
                    );
                });
    }


    @GetMapping("/{shortCode}")
    public Mono<RedirectView> expandShortUrl(@PathVariable String shortCode) {
        logger.info("CALLED TO expand {}", shortCode);
        return urlShortenerService.expandUrl(shortCode)
                .map(originalUrl -> {
                    RedirectView redirectView = new RedirectView();
                    redirectView.setUrl(originalUrl);
                    redirectView.setStatusCode(HttpStatus.FOUND);
                    logger.info("RETURNED for short {} expanded {}", shortCode, originalUrl);
                    return redirectView;
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    RedirectView redirectView = new RedirectView();
                    redirectView.setUrl("/error/not-found");
                    redirectView.setStatusCode(HttpStatus.NOT_FOUND);
                    return redirectView;
                }))
                .onErrorResume(ShortenerException.UrlExpiredException.class, ex -> {
                    RedirectView redirectView = new RedirectView();
                    redirectView.setUrl("/error/expired");
                    redirectView.setStatusCode(HttpStatus.GONE);
                    return Mono.just(redirectView);
                });
    }
}