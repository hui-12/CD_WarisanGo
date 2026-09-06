package com.warisango.service;

import com.warisango.repository.FirebaseStorageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Stores review images in the Firebase Storage bucket and returns stable Firebase download URLs.
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
    private static final String FIREBASE_DOWNLOAD_URL_PREFIX =
            "https://firebasestorage.googleapis.com/v0/b/";

    private final FirebaseStorageRepository storageRepository;
    private final long maxPhotoBytes;

    public ReviewPhotoStorageService(
            FirebaseStorageRepository storageRepository,
            @Value("${warisango.review.max-photo-size-bytes:10485760}") long maxPhotoBytes) {
        this.storageRepository = storageRepository;
        this.maxPhotoBytes = maxPhotoBytes;
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

        try (InputStream inputStream = file.getInputStream()) {
            return upload(
                    "review-photos",
                    reviewId,
                    inputStream,
                    file.getSize(),
                    imageType.contentType(),
                    imageType.extension()
            );
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the review photo.", e);
        }
    }

    public String storeAvatar(String userId, MultipartFile file) {
        ImageType imageType = detectImageType(file);
        try (InputStream inputStream = file.getInputStream()) {
            return upload(
                    "profile-images",
                    userId,
                    inputStream,
                    file.getSize(),
                    imageType.contentType(),
                    imageType.extension()).publicUrl();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read the profile image.", exception);
        }
    }

    public void delete(String photoUrl) {
        delete(photoUrl, null);
    }

    public void delete(String photoUrl, String storagePath) {
        String firebaseStoragePath = hasText(storagePath)
                ? storagePath
                : extractFirebaseStoragePath(photoUrl);

        if (firebaseStoragePath != null) {
            deleteFirebaseObject(firebaseStoragePath, photoUrl);
        }
    }

    private StoredPhoto upload(
            String folder,
            String ownerId,
            InputStream inputStream,
            long fileSize,
            String contentType,
            String extension) {

        String safeOwnerId = ownerId == null || ownerId.isBlank()
                ? "unknown-owner"
                : ownerId.replaceAll("[^a-zA-Z0-9_-]", "_");
        String storagePath = folder
                + "/"
                + safeOwnerId
                + "/"
                + UUID.randomUUID()
                + extension;
        String downloadToken = UUID.randomUUID().toString();

        try {
            storageRepository.upload(storagePath, inputStream, contentType, downloadToken);
            logger.info("Stored review photo in Firebase Storage: path={}, size={} bytes",
                    storagePath, fileSize);
            return new StoredPhoto(storagePath, buildDownloadUrl(storagePath, downloadToken));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Could not store review photo in Firebase Storage.", exception);
        }
    }

    private void deleteFirebaseObject(String storagePath, String photoUrl) {
        if (!storageRepository.isConfigured()) {
            logger.warn("Firebase Storage is not configured; skipped delete for {}", photoUrl);
            return;
        }
        storageRepository.delete(storagePath);
    }

    private String buildDownloadUrl(String storagePath, String downloadToken) {
        String encodedPath = URLEncoder.encode(storagePath, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return FIREBASE_DOWNLOAD_URL_PREFIX
                + storageRepository.getBucketName()
                + "/o/"
                + encodedPath
                + "?alt=media&token="
                + downloadToken;
    }

    private String extractFirebaseStoragePath(String photoUrl) {
        if (photoUrl == null || !storageRepository.isConfigured()) {
            return null;
        }

        String prefix = FIREBASE_DOWNLOAD_URL_PREFIX + storageRepository.getBucketName() + "/o/";
        if (!photoUrl.startsWith(prefix)) {
            return null;
        }

        String encodedPath = photoUrl.substring(prefix.length());
        int queryStart = encodedPath.indexOf('?');
        if (queryStart >= 0) {
            encodedPath = encodedPath.substring(0, queryStart);
        }

        try {
            return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            logger.warn("Could not decode Firebase review photo URL: {}", photoUrl, e);
            return null;
        }
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
                && (header[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        byte[] pngSignature = {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        };
        if (startsWith(header, pngSignature)) {
            return "image/png";
        }

        byte[] riffSignature = {'R', 'I', 'F', 'F'};
        byte[] webpSignature = {'W', 'E', 'B', 'P'};
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record StoredPhoto(String storagePath, String publicUrl) {
    }

    private record ImageType(String contentType, String extension) {
    }
}
