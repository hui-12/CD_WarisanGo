package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
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
    private final Firestore firestore;

    public BusinessRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<HeritageBusinessDTO> findApprovedBusinesses() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", "Approved")
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

    /**
     * Loads one approved business for Review pages.
     * The normal Firestore document ID is used first, with a businessId field query as a fallback.
     */
    public HeritageBusinessDTO findByBusinessId(String businessId)
            throws ExecutionException, InterruptedException {
        if (businessId == null || businessId.isBlank()) {
            return null;
        }

        DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                .document(businessId)
                .get()
                .get();

        if (!document.exists()) {
            QuerySnapshot snapshot = firestore.collection(COLLECTION_NAME)
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
        if (status != null && !status.isBlank() && !"Approved".equalsIgnoreCase(status)) {
            return null;
        }

        return toBusinessDTO(document);
    }

    // Real-time Firestore Snapshot Listener
    public ListenerRegistration addApprovedBusinessesListener(Consumer<List<HeritageBusinessDTO>> callback) {
        return firestore.collection(COLLECTION_NAME)
                .whereEqualTo("status", "Approved")
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
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(id).get().get();
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
                doc.getDouble("averageRating"),
                getCheckInPoints(doc)
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

    private HeritageBusinessDTO toBusinessDTO(DocumentSnapshot document) {
        GeoPoint geoPoint = document.getGeoPoint("location");
        double lat = geoPoint != null ? geoPoint.getLatitude() : 0.0;
        double lng = geoPoint != null ? geoPoint.getLongitude() : 0.0;
        String businessId = document.getString("businessId");

        HeritageBusinessDTO dto = toDto(document, lat, lng);
        if (businessId != null && !businessId.isBlank()) {
            dto.setBusinessId(businessId);
        }
        return dto;
    }

    private int getCheckInPoints(DocumentSnapshot doc) {
        Long points = doc.getLong("checkInPoints");
        return points != null && points > 0 ? points.intValue() : 50;
    }
}
