package com.warisango.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Converts Firestore's ISO timestamp text into a short, user-friendly date.
 */
public final class ReviewDateFormatter {

    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Kuala_Lumpur");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

    private ReviewDateFormatter() {
    }

    public static String format(Object timestampValue) {
        if (timestampValue == null) {
            return "";
        }

        String timestampText = timestampValue.toString().trim();
        if (timestampText.isEmpty()) {
            return "";
        }

        try {
            return Instant.parse(timestampText)
                    .atZone(DISPLAY_ZONE)
                    .format(DISPLAY_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Try the other common ISO formats below before returning the original value.
        }

        try {
            return OffsetDateTime.parse(timestampText)
                    .atZoneSameInstant(DISPLAY_ZONE)
                    .format(DISPLAY_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Continue with local date-time parsing.
        }

        try {
            return LocalDateTime.parse(timestampText)
                    .format(DISPLAY_FORMAT);
        } catch (DateTimeParseException ignored) {
            // Continue with date-only parsing.
        }

        try {
            return LocalDate.parse(timestampText)
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
            return timestampText;
        }
    }
}
