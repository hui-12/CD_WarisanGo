package com.warisango;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class FirestoreTestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FirestoreTestRunner.class);

    @Override
    public void run(String... args) {
        log.info(">>> Starting Firestore Connection Test...");

        try {
            // 1. Initialize Firebase directly here to ensure the code actually executes[cite: 20]
            if (FirebaseApp.getApps().isEmpty()) {
                log.info(">>> Initializing FirebaseApp...");
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                
                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId("warisango")
                        .build();

                    FirebaseApp.initializeApp(options);
                    log.info(">>> FirebaseApp successfully initialized!");
                }
            }

            // 2. Get the Firestore instance[cite: 20]
            Firestore db = FirestoreClient.getFirestore();

            // 3. Prepare test data[cite: 20]
            Map<String, Object> data = new HashMap<>();
            data.put("status", "Success!");
            data.put("message", "Hello from self-contained runner!");
            data.put("timestamp", System.currentTimeMillis());

            // 4. Write data[cite: 20]
            DocumentReference docRef = db.collection("test_connection").document("test_doc");
            ApiFuture<WriteResult> writeResult = docRef.set(data);
            log.info(">>> Write completed at: {}", writeResult.get().getUpdateTime());

            // 5. Read data[cite: 20]
            ApiFuture<DocumentSnapshot> readResult = docRef.get();
            DocumentSnapshot document = readResult.get();

            if (document.exists()) {
                log.info(">>> Read successful! Retrieved data: {}", document.getData());
            } else {
                log.warn(">>> Error: Test document was not found.");
            }

        } catch (Exception e) {
            log.error(">>> Firestore Connection Test FAILED!", e);
        }
    }
}