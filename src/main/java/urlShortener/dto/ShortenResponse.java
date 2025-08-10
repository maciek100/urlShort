package urlShortener.dto;

/**
 * @param shortUrl Getters
 */
public record ShortenResponse(String shortUrl, String status, String error) {

}
