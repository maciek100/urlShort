package urlShortener.dto;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;


public record URLAccessStats (String _id, long total, LocalDateTime firstAccess, LocalDateTime lastAccess) {}
