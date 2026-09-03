package com.warisango.service;

import com.google.cloud.Timestamp;
import com.warisango.dto.AIExtractionResponse;
import com.warisango.dto.AIExtractionResult;
import com.warisango.exception.AIProcessingException;
import com.warisango.model.AIRecord;
import com.warisango.model.ExtractedBusiness;
import com.warisango.repository.AIRecordRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIRecordService {

    private static final String PENDING_STATUS = "Pending";

    private final AIRecordRepository aiRecordRepository;

    public AIRecordService(AIRecordRepository aiRecordRepository) {
        this.aiRecordRepository = aiRecordRepository;
    }

    public String save(
            AIExtractionResult result,
            String transcript,
            String sourceUrl) {

        if (result == null || result.getBusinesses() == null) {
            throw new AIProcessingException(
                    "AI extraction did not contain a businesses collection."
            );
        }

        List<ExtractedBusiness> businesses = result.getBusinesses()
                .stream()
                .map(this::toExtractedBusiness)
                .toList();

        AIRecord record = new AIRecord(
                businesses,
                sourceUrl,
                transcript,
                PENDING_STATUS,
                Timestamp.now()
        );

        return aiRecordRepository.save(record);
    }

    private ExtractedBusiness toExtractedBusiness(AIExtractionResponse business) {
        return new ExtractedBusiness(
                valueOrEmpty(business.name()),
                valueOrEmpty(business.address()),
                valueOrEmpty(business.state()),
                valueOrEmpty(business.city()),
                valueOrEmpty(business.location()),
                valueOrEmpty(business.description()),
                valueOrEmpty(business.operatingHour())
        );
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
