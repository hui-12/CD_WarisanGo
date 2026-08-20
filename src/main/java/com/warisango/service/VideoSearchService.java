package com.warisango.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.warisango.config.YoutubeConfig;
import com.warisango.dto.VideoDTO;
import com.warisango.exception.AIProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class VideoSearchService {

    private final YoutubeConfig youtubeConfig;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public VideoSearchService(YoutubeConfig youtubeConfig) {
        this.youtubeConfig = youtubeConfig;
        this.restClient = RestClient.create();
        this.objectMapper = new ObjectMapper();
    }

    public List<VideoDTO> searchVideos(String keyword) {

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
        } catch (Exception exception) {
            throw new AIProcessingException(
                    "Unable to search YouTube videos.",
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

            dto.setVideoId(item.get("id").get("videoId").asText());

            dto.setTitle(item.get("snippet").get("title").asText());

            dto.setDescription(item.get("snippet").get("description").asText());

            dto.setChannel(item.get("snippet").get("channelTitle").asText());

            dto.setPublishedAt(item.get("snippet").get("publishedAt").asText());

            dto.setThumbnail(
                    item.get("snippet")
                            .get("thumbnails")
                            .get("high")
                            .get("url")
                            .asText()
            );

            videos.add(dto);
        }

        return videos;
    }
}
