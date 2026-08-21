package com.warisango.controller;

import com.warisango.dto.VideoDTO;
import com.warisango.model.service.VideoSearchService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoSearchService videoSearchService;

    public VideoController(VideoSearchService videoSearchService) {
        this.videoSearchService = videoSearchService;
    }

    // Endpoint to search for videos based on a keyword
    @GetMapping("/search")
    public List<VideoDTO> search(
            @RequestParam String keyword) throws Exception {

        return videoSearchService.searchVideos(keyword);
    }

}