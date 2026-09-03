package com.warisango.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YoutubeConfig {

    private final String apiKey;

    private final String baseUrl;

    public YoutubeConfig(
            @Value("${youtube.api.key:}") String apiKey,
            @Value("${youtube.base.url:https://www.googleapis.com/youtube/v3}") String baseUrl) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl.strip().replaceAll("/+$", "");
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
