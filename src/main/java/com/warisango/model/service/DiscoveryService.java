package com.warisango.model.service;

import com.warisango.dto.VideoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DiscoveryService {

    private static final Map<String, String> COMMON_SEARCH_CORRECTIONS = Map.ofEntries(
            Map.entry("malysia", "malaysia"),
            Map.entry("malaysiaa", "malaysia"),
            Map.entry("restaurent", "restaurant"),
            Map.entry("resturant", "restaurant"),
            Map.entry("restraunt", "restaurant"),
            Map.entry("heritge", "heritage"),
            Map.entry("tradisional", "traditional")
    );

    private final VideoSearchService videoSearchService;

    public DiscoveryService(
            VideoSearchService videoSearchService) {

        this.videoSearchService =
                videoSearchService;
    }

    /**
     * Searches YouTube videos using the supplied keyword.
     *
     * @param keyword search keyword
     * @return list of matching videos
     */
    public List<VideoDTO> discover(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        String normalizedKeyword = keyword
                .trim()
                .replaceAll("\\s+", " ");

        return videoSearchService.searchVideos(
                correctCommonMisspellings(normalizedKeyword)
        );
    }

    private String correctCommonMisspellings(String keyword) {
        String[] words = keyword.split(" ");

        for (int index = 0; index < words.length; index++) {
            String lowercaseWord = words[index].toLowerCase(Locale.ROOT);
            words[index] = COMMON_SEARCH_CORRECTIONS.getOrDefault(
                    lowercaseWord,
                    words[index]
            );
        }

        return String.join(" ", words);
    }
}
