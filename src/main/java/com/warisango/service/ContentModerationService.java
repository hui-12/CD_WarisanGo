package com.warisango.service;

import com.warisango.exception.ProhibitedContentException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates user-generated review and comment text using local word lists.
 * The check is deliberately kept offline so it does not require a paid API.
 */
@Service
public class ContentModerationService {

    private static final String PROHIBITED_MESSAGE =
            "Your text contains prohibited language. Please edit it before submitting.";

    private final List<String> prohibitedTerms;

    public ContentModerationService() {
        this.prohibitedTerms = loadTerms(
                "moderation/prohibited-en.txt",
                "moderation/prohibited-ms.txt",
                "moderation/prohibited-zh.txt"
        );
    }

    public void validate(String text) {
        if (containsProhibitedContent(text)) {
            throw new ProhibitedContentException(PROHIBITED_MESSAGE);
        }
    }

    public boolean containsProhibitedContent(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalizedText = normalize(text);
        String punctuationNormalizedText = removePunctuation(normalizedText);
        String compactText = removePunctuationAndWhitespace(normalizedText);

        for (String term : prohibitedTerms) {
            if (containsTerm(normalizedText, term)
                    || containsTerm(punctuationNormalizedText, term)
                    || shouldCheckCompactForm(term)
                    && compactText.contains(removePunctuationAndWhitespace(term))) {
                return true;
            }
        }

        return false;
    }

    public String getProhibitedMessage() {
        return PROHIBITED_MESSAGE;
    }

    private List<String> loadTerms(String... resources) {
        List<String> terms = new ArrayList<>();

        for (String resourcePath : resources) {
            ClassPathResource resource = new ClassPathResource(resourcePath);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String term = normalize(line);
                    if (!term.isBlank() && !term.startsWith("#")) {
                        terms.add(term);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not load moderation word list: " + resourcePath, e);
            }
        }

        return List.copyOf(terms);
    }

    private boolean containsTerm(String text, String term) {
        if (text.isBlank() || term.isBlank()) {
            return false;
        }

        if (containsCjk(term)) {
            return text.contains(term);
        }

        String expression = "(?<!\\p{L})" + Pattern.quote(term) + "(?!\\p{L})";
        return Pattern.compile(expression).matcher(text).find();
    }

    /**
     * Compact matching is useful for terms written with spaces or symbols between
     * characters, but short English terms must not be matched inside normal words.
     * For example, "ass" must not match the word "class".
     */
    private boolean shouldCheckCompactForm(String term) {
        return containsCjk(term) || removePunctuationAndWhitespace(term).length() >= 4;
    }

    private String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String removePunctuation(String value) {
        return value.replaceAll("[\\p{P}\\p{S}]+", "");
    }

    private String removePunctuationAndWhitespace(String value) {
        return value.replaceAll("[\\p{P}\\p{S}\\s]+", "");
    }

    private boolean containsCjk(String value) {
        return value.codePoints().anyMatch(codePoint ->
                (codePoint >= 0x3400 && codePoint <= 0x4DBF)
                        || (codePoint >= 0x4E00 && codePoint <= 0x9FFF)
                        || (codePoint >= 0xF900 && codePoint <= 0xFAFF));
    }
}
