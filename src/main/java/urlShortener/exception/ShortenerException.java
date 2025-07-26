package urlShortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ShortenerException {
    /**
     * Custom exceptions for URLShortener service
     */

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidUrlException extends RuntimeException {
        public InvalidUrlException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class UrlNotFoundException extends RuntimeException {
        public UrlNotFoundException(String shortCode) {
            super("Short URL not found: " + shortCode);
        }
    }

    @ResponseStatus(HttpStatus.GONE)
    public static class UrlExpiredException extends RuntimeException {
        public UrlExpiredException(String shortCode) {
            super("Short URL has expired: " + shortCode);
        }
    }

}
