package com.warisango.config;

import com.warisango.dto.ReviewPhotoDTO;
import com.warisango.model.repository.ReviewPhotoRepository;
import com.warisango.model.service.ReviewPhotoStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * One-time migration for review photo documents that still point to the old local upload directory.
 * Local files are intentionally retained until the developer verifies the Firebase copies.
 */
@Component
@ConditionalOnProperties({
        @ConditionalOnProperty(
                name = "firebase.enabled",
                havingValue = "true",
                matchIfMissing = true
        ),
        @ConditionalOnProperty(
                name = "warisango.review.migrate-local-photos",
                havingValue = "true"
        )
})
public class ReviewPhotoMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ReviewPhotoMigrationRunner.class);

    private final ReviewPhotoRepository reviewPhotoRepository;
    private final ReviewPhotoStorageService storageService;

    public ReviewPhotoMigrationRunner(
            ReviewPhotoRepository reviewPhotoRepository,
            ReviewPhotoStorageService storageService) {
        this.reviewPhotoRepository = reviewPhotoRepository;
        this.storageService = storageService;
    }

    @Override
    public void run(String... args) {
        int migratedCount = 0;

        for (ReviewPhotoDTO photo : reviewPhotoRepository.findAll()) {
            Path source = storageService.resolveLegacyPath(photo.getPhotoUrl());
            if (source == null || !Files.isRegularFile(source)) {
                continue;
            }

            ReviewPhotoStorageService.StoredPhoto uploaded = null;
            try {
                uploaded = storageService.migrate(photo.getReviewId(), source);
                photo.setPhotoUrl(uploaded.publicUrl());
                photo.setStoragePath(uploaded.storagePath());
                reviewPhotoRepository.save(photo);
                migratedCount++;
            } catch (RuntimeException e) {
                if (uploaded != null) {
                    storageService.delete(uploaded.publicUrl(), uploaded.storagePath());
                }
                logger.error("Could not migrate legacy review photo {}", photo.getPhotoId(), e);
            }
        }

        logger.info("Review photo migration completed. Migrated {} photo(s).", migratedCount);
    }
}
