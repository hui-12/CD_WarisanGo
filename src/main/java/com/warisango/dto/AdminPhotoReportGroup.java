package com.warisango.dto;

import java.util.List;

public record AdminPhotoReportGroup(
        String status,
        String label,
        List<BusinessPhotoReportView> reports
) {
}
