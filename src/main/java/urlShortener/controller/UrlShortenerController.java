package urlShortener.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.InvalidUrlException;
import urlShortener.dto.ShortenRequest;
import urlShortener.dto.ShortenResponse;
import urlShortener.exception.ShortenerException;
import urlShortener.service.UrlShortenerService;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1")
public class UrlShortenerController {
    private final Logger logger = Logger.getLogger(UrlShortenerController.class.getName());
    @Autowired
    private UrlShortenerService urlShortService;

    @GetMapping("/alive")
    public ResponseEntity<String> checkAlive () {
        return ResponseEntity.ok("Long Live the Queen!");
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> shortenLongUrl(@Valid @RequestBody ShortenRequest request) {
        try {
            String shortUrl = urlShortService.shortenUrl(request.getUrl());
            logger.log(Level.INFO, "Received " + request.getUrl() + " returned " + shortUrl);
            return ResponseEntity.ok(new ShortenResponse(shortUrl, "success", null));
        } catch (InvalidUrlException e) {
            logger.log(Level.SEVERE, "Exception in Controller: " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ShortenResponse(null, "error", e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in Controller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ShortenResponse(null, "error", "Internal server error"));
        }
    }


    @GetMapping("/{shortCode}")
    public RedirectView expandShortUrl(@PathVariable String shortCode) {
        System.out.println("CALLED TO expand " + shortCode);
        RedirectView redirectView = new RedirectView();
        try {
            String originalUrl = urlShortService.expandUrl(shortCode);
            if (originalUrl != null) {
                redirectView.setUrl(originalUrl); //= new RedirectView(originalUrl);
                redirectView.setStatusCode(HttpStatus.FOUND); // 302 redirect
            } else {
                redirectView.setUrl("/error/not-found");
                redirectView.setStatusCode(HttpStatus.NOT_FOUND);
            }
        } catch (ShortenerException.UrlNotFoundException e) {
            // Redirect to a "not found" page or return error
            //RedirectView redirectView = new RedirectView();
            redirectView.setUrl("/error/not-found");
            redirectView.setStatusCode(HttpStatus.NOT_FOUND);
        } catch (ShortenerException.UrlExpiredException e) {
            // Redirect to an "expired" page
            redirectView.setUrl("/error/expired");
            redirectView.setStatusCode(HttpStatus.GONE);
        }
        return redirectView;
    }
}
