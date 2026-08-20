package com.warisango.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public FirebaseApp firebaseApp() {
        try (
                InputStream serviceAccount = new ClassPathResource(
                        "firebase-service-account.json"
                ).getInputStream()
        ) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId("warisango")
                .build();

            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
            logger.info("Firebase initialized successfully for project: warisango");
            return firebaseApp;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to initialize Firebase.",
                    e
            );
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
