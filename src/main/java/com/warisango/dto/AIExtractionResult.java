package com.warisango.dto;

import java.util.List;

public class AIExtractionResult {

    private List<AIExtractionResponse> businesses;

    public List<AIExtractionResponse> getBusinesses() {
        return businesses;
    }

    public void setBusinesses(List<AIExtractionResponse> businesses) {
        this.businesses = businesses;
    }
}