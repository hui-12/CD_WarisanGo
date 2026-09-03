package com.warisango.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

    @Mock
    private VideoSearchService videoSearchService;

    @InjectMocks
    private DiscoveryService discoveryService;

    @Test
    void discoverNormalizesWhitespaceAndCommonMisspellings() {
        discoveryService.discover("  malysia   history   restaurent  ");

        verify(videoSearchService)
                .searchVideos("malaysia history restaurant");
    }
}
