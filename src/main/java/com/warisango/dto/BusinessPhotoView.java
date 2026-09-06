package com.warisango.dto;

public record BusinessPhotoView(
        String photoId, String businessId, String imageUrl, String uploaderId,
        String uploaderName, String uploadedAtDisplay) {
}
