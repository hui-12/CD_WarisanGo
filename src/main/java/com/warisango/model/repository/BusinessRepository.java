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
                    doc.getDouble("averageRating"),
                    getCheckInPoints(doc)
            );
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

                        HeritageBusinessDTO dto = new HeritageBusinessDTO(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("address"),
                                doc.getString("state"),
                                doc.getString("city"),
                                doc.getString("description"),
                                lat,
                                lng,
                                doc.getDouble("averageRating"),
                                getCheckInPoints(doc)
                        );
                        list.add(dto);
                    }
                    callback.accept(list);
                });
    }
    private int getCheckInPoints(DocumentSnapshot doc) {
        Long points = doc.getLong("checkInPoints");
        return points != null && points > 0 ? points.intValue() : 50;
    }
}