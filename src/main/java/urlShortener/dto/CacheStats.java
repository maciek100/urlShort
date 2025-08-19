package urlShortener.dto;

public record CacheStats(int size, int httpResult, String message) {}
