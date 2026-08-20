package com.warisango.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

/**
 * Stores review images on the Spring Boot host at no additional service cost.
 * Firestore stores only the public application URL, never the binary image.
 */
@Service
public class ReviewPhotoStorageService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewPhotoStorageService.class);

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private static final int MAX_PHOTOS_PER_REVIEW = 10;

    private final Path storageDirectory;
    private final String publicUrlPrefix;
    private final long maxPhotoBytes;

    public ReviewPhotoStorageService(
            @Value("${warisango.review.upload-directory:uploads/reviews}") String uploadDirectory,
            @Value("${warisango.review.upload-url-prefix:/uploads/reviews}") String publicUrlPrefix,
            @Value("${warisango.review.max-photo-size-bytes:10485760}") long maxPhotoBytes) {

        this.storageDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.publicUrlPrefix = normalizeUrlPrefix(publicUrlPrefix);
        this.maxPhotoBytes = maxPhotoBytes;

        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create review photo directory: " + storageDirectory, e);
        }
    }

    public void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        long nonEmptyFileCount = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .count();

        if (nonEmptyFileCount > MAX_PHOTOS_PER_REVIEW) {
            throw new IllegalArgumentException("You can upload a maximum of 10 photos per review.");
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            validateFile(file);
        }
    }

    public StoredPhoto store(String reviewId, MultipartFile file) {
        ImageType imageType = detectImageType(file);

        String safeReviewId = reviewId.replaceAll("[^a-zA-Z0-9_-]", "_");
        Path destination = null;

        try (InputStream inputStream = file.getInputStream()) {
            destination = Files.createTempFile(storageDirectory, safeReviewId + "_", imageType.extension());
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            String fileName = destination.getFileName().toString();
            return new StoredPhoto(destination, publicUrlPrefix + "/" + fileName);
        } catch (IOException e) {
            if (destination != null) {
                try {
                    Files.deleteIfExists(destination);
                } catch (IOException cleanupException) {
                    logger.warn("Could not clean up failed review photo upload: {}", destination,
                            cleanupException);
                }
            }
            throw new IllegalStateException("Could not store review photo.", e);
        }
    }

    public void delete(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }

        String fileName = extractFileName(photoUrl);
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        Path file = storageDirectory.resolve(fileName).normalize();
        if (!file.startsWith(storageDirectory)) {
            logger.warn("Skipped review photo outside upload directory: {}", photoUrl);
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Could not delete local review photo: {}", photoUrl, e);
        }
    }

    public Path getStorageDirectory() {
        return storageDirectory;
    }

    private void validateFile(MultipartFile file) {
        detectImageType(file);
    }

    private ImageType detectImageType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one image.");
        }

        if (file.getSize() > maxPhotoBytes) {
            throw new IllegalArgumentException("Each review photo must be 10 MB or smaller.");
        }

        byte[] header;
        try (InputStream inputStream = file.getInputStream()) {
            header = inputStream.readNBytes(12);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read the uploaded image.", e);
        }

        String detectedContentType = detectContentType(header);
        if (detectedContentType == null) {
            throw new IllegalArgumentException("Only JPG, PNG, and WebP images are allowed.");
        }

        String declaredContentType = file.getContentType();
        if (!isCompatibleContentType(declaredContentType, detectedContentType)) {
            throw new IllegalArgumentException("The uploaded file is not a valid JPG, PNG, or WebP image.");
        }

        return new ImageType(detectedContentType, ALLOWED_TYPES.get(detectedContentType));
    }

    private String detectContentType(byte[] header) {
        if (header.length >= 3
                && (header[0] & 0xFF) == 0xFF
                && (header[1] & 0xFF) == 0xD8
                && (header[2] & 0xFF) == 0xFF
        ) {
            return "image/jpeg";
        }

        byte[] pngSignature = {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };
        if (startsWith(header, pngSignature)) {
            return "image/png";
        }

        byte[] riffSignature = { 'R', 'I', 'F', 'F' };
        byte[] webpSignature = { 'W', 'E', 'B', 'P' };
        if (header.length >= 12
                && startsWith(header, riffSignature)
                && startsWith(header, webpSignature, 8)) {
            return "image/webp";
        }

        return null;
    }

    private boolean isCompatibleContentType(String declaredContentType, String detectedContentType) {
        if (declaredContentType == null || declaredContentType.isBlank()) {
            return true;
        }

        String normalized = declaredContentType.toLowerCase();
        return "application/octet-stream".equals(normalized)
                || detectedContentType.equals(normalized)
                || ("image/jpeg".equals(detectedContentType) && "image/jpg".equals(normalized));
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        return startsWith(value, prefix, 0);
    }

    private boolean startsWith(byte[] value, byte[] prefix, int offset) {
        if (value.length < offset + prefix.length) {
            return false;
        }

        for (int index = 0; index < prefix.length; index++) {
            if (value[offset + index] != prefix[index]) {
                return false;
            }
        }

        return true;
    }

    private String extractFileName(String photoUrl) {
        String prefix = publicUrlPrefix + "/";
        if (!photoUrl.startsWith(prefix)) {
            return null;
        }

        String fileName = photoUrl.substring(prefix.length());
        return fileName.contains("/") ? null : fileName;
    }

    private String normalizeUrlPrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "/uploads/reviews";
        }

        String normalized = prefix.startsWith("/") ? prefix : "/" + prefix;
        return normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    public record StoredPhoto(Path path, String publicUrl) {
    }

    private record ImageType(String contentType, String extension) {
    }
}
