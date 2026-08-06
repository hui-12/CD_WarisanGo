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
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class FirestoreTestRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println(">>> Starting Firestore Connection Test...");

        try {
            // 1. Initialize Firebase directly here to ensure the code actually executes
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println(">>> Initializing FirebaseApp...");
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("warisango")
                    .build();

                FirebaseApp.initializeApp(options);
                System.out.println(">>> FirebaseApp successfully initialized!");
            }

            // 2. Get the Firestore instance
            Firestore db = FirestoreClient.getFirestore();

            // 3. Prepare test data
            Map<String, Object> data = new HashMap<>();
            data.put("status", "Success!");
            data.put("message", "Hello from self-contained runner!");
            data.put("timestamp", System.currentTimeMillis());

            // 4. Write data
            DocumentReference docRef = db.collection("test_connection").document("test_doc");
            ApiFuture<WriteResult> writeResult = docRef.set(data);
            System.out.println(">>> Write completed at: " + writeResult.get().getUpdateTime());

            // 5. Read data
            ApiFuture<DocumentSnapshot> readResult = docRef.get();
            DocumentSnapshot document = readResult.get();

            if (document.exists()) {
                System.out.println(">>> Read successful! Retrieved data: " + document.getData());
            } else {
                System.out.println(">>> Error: Test document was not found.");
            }

        } catch (Exception e) {
            System.err.println(">>> Firestore Connection Test FAILED!");
            e.printStackTrace();
        }
    }
}
