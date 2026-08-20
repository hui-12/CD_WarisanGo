package com.warisango.controller;

import com.warisango.dto.VideoDTO;
import com.warisango.service.DiscoveryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(
            DiscoveryService discoveryService) {

        this.discoveryService =
                discoveryService;
    }

    @PostMapping("/search")
    public List<VideoDTO> search(
            @RequestBody Map<String, String> request)
            throws Exception {

        return discoveryService.discover(
                request.get("keyword")
        );
    }
}
