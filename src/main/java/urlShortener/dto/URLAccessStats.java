package urlShortener.dto;

import java.time.LocalDateTime;


public record URLAccessStats (String _id, long total, LocalDateTime firstAccess, LocalDateTime lastAccess) {}
