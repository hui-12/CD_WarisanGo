package com.warisango.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Schema;
import com.warisango.dto.AIExtractionResult;
import com.warisango.exception.AIProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AIExtractionService {

    private static final Logger logger =
            LoggerFactory.getLogger(AIExtractionService.class);

    private final Client geminiClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public AIExtractionService(
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.model}") String model,
            ObjectMapper objectMapper) {

        this.geminiClient = Client.builder()
                .apiKey(apiKey)
                .build();

        this.objectMapper = objectMapper;
        this.model = model;
    }

    /**
     * Extracts one or more heritage businesses from the transcript.
     */
    public AIExtractionResult extract(String transcript) {

        if (transcript == null || transcript.isBlank()) {
            throw new AIProcessingException(
                    "Transcript cannot be empty."
            );
        }

        try {

            String prompt = buildPrompt(transcript);

            GenerateContentConfig config =
                    GenerateContentConfig.builder()
                            .responseMimeType("application/json")
                            .responseSchema(buildResponseSchema())
                            .temperature(0.0f)
                            .build();

            GenerateContentResponse response =
                    geminiClient.models.generateContent(
                            model,
                            prompt,
                            config
                    );

            String json = response.text();

            if (json == null || json.isBlank()) {
                throw new AIProcessingException(
                        "Gemini returned an empty response."
                );
            }

            logger.info("Gemini extraction completed.");

            return objectMapper.readValue(
                    json,
                    AIExtractionResult.class
            );

        } catch (Exception exception) {

            logger.error(
                    "AI extraction failed.",
                    exception
            );

            throw new AIProcessingException(
                    "AI extraction failed.",
                    exception
            );
        }
    }

    /**
     * Prompt for Gemini.
     */
    private String buildPrompt(String transcript) {

        return """
                You are WarisanGo's Malaysian heritage food extraction AI.

                Analyse the transcript and identify ALL heritage food businesses.

                Rules:
                - Return every business mentioned.
                - Do not invent information.
                - Use null when unavailable.
                - Do not guess coordinates.
                - Output JSON only.
                - Allow to do research if needed to find the business information.
                - Never return an empty array. If no businesses are found, return 
                an array with a single object with all fields set to null.
                - Do not invent information.
                - Only return longitude and latitude for the location field, otherwise return null.

                Required JSON format:

                {
                  "businesses": [
                    {
                      "name": "",
                      "address": "",
                      "state": "",
                      "city": "",
                      "location": "",
                      "description": "",
                      "operatingHour": ""
                    }
                  ]
                }

                Transcript:
                %s
                """.formatted(transcript);
    }

    /**
     * Expected Gemini JSON schema.
     */
    private Schema buildResponseSchema() {

        Schema stringField = Schema.builder()
                .type("STRING")
                .build();

        Schema business = Schema.builder()
                .type("OBJECT")
                .properties(
                        Map.of(
                                "name", stringField,
                                "address", stringField,
                                "state", stringField,
                                "city", stringField,
                                "location", stringField,
                                "description", stringField,
                                "operatingHour", stringField
                        )
                )
                .required(
                        List.of(
                                "name",
                                "address",
                                "state",
                                "city",
                                "location",
                                "description",
                                "operatingHour"
                        )
                )
                .build();

        Schema businessArray = Schema.builder()
                .type("ARRAY")
                .items(business)
                .build();

        return Schema.builder()
                .type("OBJECT")
                .properties(
                        Map.of(
                                "businesses",
                                businessArray
                        )
                )
                .required(List.of("businesses"))
                .build();
    }
}
