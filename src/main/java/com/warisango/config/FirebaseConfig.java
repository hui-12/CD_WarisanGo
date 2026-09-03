package com.warisango.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@ConditionalOnProperty(
        name = "firebase.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class FirebaseConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean(destroyMethod = "delete")
    public FirebaseApp firebaseApp(
            @Value("${warisango.firebase.credentials-location:classpath:firebase-service-account.json}")
            Resource credentialsResource,
            @Value("${warisango.firebase.project-id}") String projectId,
            @Value("${warisango.firebase.storage-bucket}") String storageBucket) {
        try (InputStream serviceAccount = credentialsResource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId(projectId)
                .setStorageBucket(storageBucket)
                .build();

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            logger.info("Firebase initialized successfully for project: {}", projectId);
            return firebaseApp;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to initialize Firebase. Verify the configured credentials resource.",
                    exception
            );
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
