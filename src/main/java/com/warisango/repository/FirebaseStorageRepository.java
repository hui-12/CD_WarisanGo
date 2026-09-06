package com.warisango.repository;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.Map;

@Repository
public class FirebaseStorageRepository {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageRepository.class);
    private final Bucket bucket;

    public FirebaseStorageRepository(
            ObjectProvider<FirebaseApp> firebaseAppProvider,
            @Value("${warisango.firebase.storage-bucket}") String bucketName) {
        FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
        if (firebaseApp == null) {
            this.bucket = null;
            return;
        }
        StorageClient storageClient = StorageClient.getInstance(firebaseApp);
        this.bucket = bucketName == null || bucketName.isBlank()
                ? storageClient.bucket()
                : storageClient.bucket(bucketName);
    }

    public void upload(String storagePath, InputStream inputStream, String contentType, String downloadToken) {
        requireConfigured();
        Blob blob = null;
        try {
            blob = bucket.create(storagePath, inputStream, contentType);
            blob.toBuilder()
                    .setMetadata(Map.of("firebaseStorageDownloadTokens", downloadToken))
                    .build()
                    .update();
        } catch (RuntimeException exception) {
            if (blob != null) {
                try {
                    blob.delete();
                } catch (RuntimeException cleanupException) {
                    logger.warn("Could not clean up failed Firebase upload: {}", storagePath, cleanupException);
                }
            }
            throw new IllegalStateException("Could not store the file in Firebase Storage.", exception);
        }
    }

    public void delete(String storagePath) {
        if (bucket == null) {
            logger.warn("Firebase Storage is not configured; skipped delete for {}", storagePath);
            return;
        }
        try {
            Blob blob = bucket.get(storagePath);
            if (blob != null) {
                blob.delete();
            }
        } catch (RuntimeException exception) {
            logger.warn("Could not delete Firebase object: {}", storagePath, exception);
        }
    }

    public String getBucketName() {
        requireConfigured();
        return bucket.getName();
    }

    public boolean isConfigured() {
        return bucket != null;
    }

    private void requireConfigured() {
        if (bucket == null) {
            throw new IllegalStateException(
                    "Firebase Storage is not configured. Enable Firebase and configure the service account."
            );
        }
    }
}
