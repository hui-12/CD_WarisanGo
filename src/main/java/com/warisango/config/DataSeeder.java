// package com.warisango.config;

// import com.google.cloud.firestore.CollectionReference;
// import com.google.cloud.firestore.FieldValue;
// import com.google.cloud.firestore.GeoPoint;
// import com.google.cloud.firestore.Firestore;
// import com.google.firebase.cloud.FirestoreClient;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import java.util.Arrays;
// import java.util.HashMap;
// import java.util.Map;

// @Component
// public class DataSeeder implements CommandLineRunner {

//     private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

//     @Override
//     public void run(String... args) throws Exception {
//         Firestore db = FirestoreClient.getFirestore();
//         CollectionReference colRef = db.collection("HeritageBusinesses");

//         // Prevent duplicate insertions
//         if (!colRef.limit(1).get().get().isEmpty()) {
//             logger.info("Collection 'HeritageBusinesses' already contains data. Skipping automatic seeding.");
//             return;
//         }

//         logger.info("Seeding initial heritage business dummy records to Firestore...");

//         // Record 1: KL - Yut Kee
//         Map<String, Object> b1 = new HashMap<>();
//         b1.put("businessId", "hb_001");
//         b1.put("name", "Yut Kee Restaurant");
//         b1.put("address", "7, Jalan Kamunting, Chow Kit");
//         b1.put("state", "Kuala Lumpur");
//         b1.put("city", "Kuala Lumpur");
//         b1.put("description", "Classic Hainanese coffeeshop serving traditional chicken chop and kaya toast since 1928.");
//         b1.put("location", new GeoPoint(3.1568, 101.7001));
//         b1.put("averageRating", 4.5);
//         b1.put("status", "APPROVED");
//         b1.put("sourceVideoLink", "https://www.youtube.com/watch?v=sample1");
//         b1.put("createdAt", FieldValue.serverTimestamp());

//         // Record 2: KL - Kim Lian Kee
//         Map<String, Object> b2 = new HashMap<>();
//         b2.put("businessId", "hb_002");
//         b2.put("name", "Restoran Kim Lian Kee");
//         b2.put("address", "49, Jalan Petaling, City Centre");
//         b2.put("state", "Kuala Lumpur");
//         b2.put("city", "Kuala Lumpur");
//         b2.put("description", "Famous birthplace of charcoal-fried Hokkien Mee in Petaling Street operating since 1927.");
//         b2.put("location", new GeoPoint(3.1447, 101.6970));
//         b2.put("averageRating", 4.3);
//         b2.put("status", "APPROVED");
//         b2.put("sourceVideoLink", "https://www.youtube.com/watch?v=sample2");
//         b2.put("createdAt", FieldValue.serverTimestamp());

//         // Record 3: Penang - Line Clear
//         Map<String, Object> b3 = new HashMap<>();
//         b3.put("businessId", "hb_003");
//         b3.put("name", "Nasi Kandar Line Clear");
//         b3.put("address", "177, Jalan Penang, George Town");
//         b3.put("state", "Penang");
//         b3.put("city", "George Town");
//         b3.put("description", "Iconic Penang alleyway Nasi Kandar serving rich curries and spiced meats since the 1930s.");
//         b3.put("location", new GeoPoint(5.4201, 100.3327));
//         b3.put("averageRating", 4.6);
//         b3.put("status", "APPROVED");
//         b3.put("sourceVideoLink", "https://www.youtube.com/watch?v=sample3");
//         b3.put("createdAt", FieldValue.serverTimestamp());

//         for (Map<String, Object> doc : Arrays.asList(b1, b2, b3)) {
//             colRef.document((String) doc.get("businessId")).set(doc).get();
//         }

//         logger.info("Successfully added 3 dummy heritage businesses to Firestore!");
//     }
// }