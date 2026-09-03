package com.warisango.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.warisango.config.YoutubeConfig;
import com.warisango.dto.VideoDTO;
import com.warisango.exception.AIProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoSearchService {

    private final YoutubeConfig youtubeConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public VideoSearchService(YoutubeConfig youtubeConfig, ObjectMapper objectMapper) {
        this.youtubeConfig = youtubeConfig;
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public List<VideoDTO> searchVideos(String keyword) {

        if (youtubeConfig.getApiKey().isBlank()) {
            throw new AIProcessingException(
                    "YouTube API key is missing. Set the YOUTUBE_API_KEY environment variable and restart the app."
            );
        }

        try {

            String url = UriComponentsBuilder
                .fromUriString(youtubeConfig.getBaseUrl() + "/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", 20)
                .queryParam("q", keyword)
                .queryParam("key", youtubeConfig.getApiKey())
                .build()
                .encode()
                .toUriString();

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            return parseVideos(response);
        } catch (RestClientResponseException exception) {
            throw new AIProcessingException(
                    "YouTube API request failed (HTTP " + exception.getStatusCode().value()
                            + "): " + exception.getResponseBodyAsString(),
                    exception
            );
        } catch (AIProcessingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AIProcessingException(
                    "Unable to search YouTube videos: " + exception.getMessage(),
                    exception
            );
        }
    }

    // Parse the JSON response and extract video details(no content)
    private List<VideoDTO> parseVideos(String json) throws Exception {

        List<VideoDTO> videos = new ArrayList<>();

        JsonNode root = objectMapper.readTree(json);

        JsonNode items = root.path("items");

        for (JsonNode item : items) {

            VideoDTO dto = new VideoDTO();

            dto.setVideoId(item.path("id").path("videoId").stringValue(""));

            dto.setTitle(item.path("snippet").path("title").stringValue(""));

            dto.setDescription(item.path("snippet").path("description").stringValue(""));

            dto.setChannel(item.path("snippet").path("channelTitle").stringValue(""));

            dto.setPublishedAt(item.path("snippet").path("publishedAt").stringValue(""));

            dto.setThumbnail(
                    item.path("snippet")
                            .path("thumbnails")
                            .path("high")
                            .path("url")
                            .stringValue("")
            );

            videos.add(dto);
        }

        return videos;
    }
}
