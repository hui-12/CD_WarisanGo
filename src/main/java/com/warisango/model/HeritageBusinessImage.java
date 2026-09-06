package com.warisango.model;

import java.time.Instant;

/** Represents one user-contributed photo in heritageBusinessImages. */
public record HeritageBusinessImage(
        String photoId, String businessId, String imageUrl, String storagePath,
        String uploadedBy, Instant uploadedAt, long displayOrder, String status) {
}
