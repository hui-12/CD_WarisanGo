package com.warisango.dto;

public class VideoDTO {

    private String videoId;
    private String title;
    private String description;
    private String channel;
    private String thumbnail;
    private String publishedAt;

    public VideoDTO() {
    }

    public VideoDTO(String videoId,
                    String title,
                    String description,
                    String channel,
                    String thumbnail,
                    String publishedAt) {

        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.channel = channel;
        this.thumbnail = thumbnail;
        this.publishedAt = publishedAt;
    }

    // getters & setters
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }
}