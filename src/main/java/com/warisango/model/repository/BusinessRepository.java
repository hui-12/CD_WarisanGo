package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.dto.HeritageBusinessDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Repository
public class BusinessRepository {

    private static final String COLLECTION_NAME = "HeritageBusinesses";
    private static final Logger logger = LoggerFactory.getLogger(BusinessRepository.class);

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

            HeritageBusinessDTO dto = toDto(doc, lat, lng);
            list.add(dto);
        }
        return list;
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

                        HeritageBusinessDTO dto = toDto(doc, lat, lng);
                        list.add(dto);
                    }
                    callback.accept(list);
                });
    }

    // Find single business by document id
    public Optional<HeritageBusinessDTO> findById(String id) {
        Firestore db = FirestoreClient.getFirestore();
        try {
            DocumentSnapshot doc = db.collection(COLLECTION_NAME).document(id).get().get();
            if (doc == null || !doc.exists()) {
                return Optional.empty();
            }

            GeoPoint geoPoint = doc.getGeoPoint("location");
            double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
            double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;

            HeritageBusinessDTO dto = toDto(doc, lat, lng);

            return Optional.of(dto);
        } catch (Exception e) {
            logger.error("Error fetching business by id: {}", id, e);
            return Optional.empty();
        }
    }

    private HeritageBusinessDTO toDto(DocumentSnapshot doc, double latitude, double longitude) {
        HeritageBusinessDTO dto = new HeritageBusinessDTO(
                doc.getId(),
                doc.getString("name"),
                doc.getString("address"),
                doc.getString("state"),
                doc.getString("city"),
                doc.getString("description"),
                latitude,
                longitude,
                doc.getDouble("averageRating")
        );
        dto.setCategory(doc.getString("category"));

        Object photos = doc.get("photos");
        if (photos instanceof List<?> photoList) {
            dto.setPhotos(photoList.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList());
        }
        return dto;
    }
}
