package com.warisango.dto;

import java.util.List;

public record AdminBusinessView(
        String businessId,
        String name,
        String status,
        HeritageBusinessUpdateRequest details,
        List<BusinessPhotoView> photos) {
}
