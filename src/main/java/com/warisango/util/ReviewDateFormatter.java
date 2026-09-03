package com.warisango.util;

import com.google.cloud.Timestamp;

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

    public static String format(Timestamp timestamp) {
        return timestamp == null ? "" : format(timestamp.toDate().toInstant());
    }

    public static String format(Instant instant) {
        return instant == null ? "" : instant.atZone(DISPLAY_ZONE).format(DISPLAY_FORMAT);
    }

    public static String format(String timestampText) {
        return formatText(timestampText);
    }

    /**
     * Compatibility entry point for Firestore values whose stored type may differ between old records.
     */
    public static String format(Object timestampValue) {
        if (timestampValue == null) {
            return "";
        }

        if (timestampValue instanceof Timestamp timestamp) {
            return format(timestamp);
        }
        if (timestampValue instanceof Instant instant) {
            return format(instant);
        }

        return formatText(timestampValue.toString());
    }

    private static String formatText(String value) {
        if (value == null) {
            return "";
        }

        String timestampText = value.trim();
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
