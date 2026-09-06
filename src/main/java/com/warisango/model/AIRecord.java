package com.warisango.model;

import com.google.cloud.Timestamp;

import java.util.List;

/**
 * Firestore document containing one Gemini heritage extraction result.
 */
public class AIRecord {

    private List<ExtractedBusiness> extractedBusinesses;
    private String sourceUrl;
    private String transcript;
    private String status;
    private Timestamp createdAt;

    public AIRecord() {
    }

    public AIRecord(
            List<ExtractedBusiness> extractedBusinesses,
            String sourceUrl,
            String transcript,
            String status,
            Timestamp createdAt) {

        this.extractedBusinesses = extractedBusinesses;
        this.sourceUrl = sourceUrl;
        this.transcript = transcript;
        this.status = status;
        this.createdAt = createdAt;
    }

    public List<ExtractedBusiness> getExtractedBusinesses() {
        return extractedBusinesses;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setExtractedBusinesses(List<ExtractedBusiness> extractedBusinesses) {
        this.extractedBusinesses = extractedBusinesses;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
