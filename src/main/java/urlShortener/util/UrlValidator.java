package urlShortener.util;

import java.util.regex.Pattern;

public class UrlValidator {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[\\w.-]+(?:\\.[\\w\\.-]+)+[/#?]?.*$",
            Pattern.CASE_INSENSITIVE
    );

    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }
}