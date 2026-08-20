package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.dto.HeritageBusinessDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Repository
public class BusinessRepository {

    private static final String COLLECTION_NAME = "HeritageBusinesses";

    public List<HeritageBusinessDTO> findApprovedBusinesses() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "APPROVED")
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<HeritageBusinessDTO> list = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            GeoPoint geoPoint = doc.getGeoPoint("location");
            double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
            double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;

            HeritageBusinessDTO dto = new HeritageBusinessDTO(
                    doc.getId(),
                    doc.getString("name"),
                    doc.getString("address"),
                    doc.getString("state"),
                    doc.getString("city"),
                    doc.getString("description"),
                    lat,
                    lng,
                    doc.getDouble("averageRating")
            );
            list.add(dto);
        }
        return list;
    }

    /**
     * Loads one approved business for Review pages.
     * The normal Firestore document ID is used first, with a businessId field query as a fallback.
     */
    public HeritageBusinessDTO findByBusinessId(String businessId)
            throws ExecutionException, InterruptedException {
        if (businessId == null || businessId.isBlank()) {
            return null;
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot document = db.collection(COLLECTION_NAME)
                .document(businessId)
                .get()
                .get();

        if (!document.exists()) {
            QuerySnapshot snapshot = db.collection(COLLECTION_NAME)
                    .whereEqualTo("businessId", businessId)
                    .limit(1)
                    .get()
                    .get();

            if (snapshot.isEmpty()) {
                return null;
            }

            document = snapshot.getDocuments().get(0);
        }

        String status = document.getString("status");
        if (status != null && !status.isBlank() && !"APPROVED".equalsIgnoreCase(status)) {
            return null;
        }

        return toBusinessDTO(document);
    }

    // Real-time Firestore Snapshot Listener
    public ListenerRegistration addApprovedBusinessesListener(Consumer<List<HeritageBusinessDTO>> callback) {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "APPROVED")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) {
                        return;
                    }

                    List<HeritageBusinessDTO> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        GeoPoint geoPoint = doc.getGeoPoint("location");
                        double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
                        double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;

                        HeritageBusinessDTO dto = new HeritageBusinessDTO(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("address"),
                                doc.getString("state"),
                                doc.getString("city"),
                                doc.getString("description"),
                                lat,
                                lng,
                                doc.getDouble("averageRating")
                        );
                        list.add(dto);
                    }
                    callback.accept(list);
        });
    }

    private HeritageBusinessDTO toBusinessDTO(DocumentSnapshot document) {
        GeoPoint geoPoint = document.getGeoPoint("location");
        double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
        double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;
        String businessId = document.getString("businessId");

        return new HeritageBusinessDTO(
                businessId == null || businessId.isBlank() ? document.getId() : businessId,
                document.getString("name"),
                document.getString("address"),
                document.getString("state"),
                document.getString("city"),
                document.getString("description"),
                lat,
                lng,
                document.getDouble("averageRating")
        );
    }
}
