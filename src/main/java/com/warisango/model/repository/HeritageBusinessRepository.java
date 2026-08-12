package com.warisango.model.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.warisango.dto.HeritageBusinessDTO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class HeritageBusinessRepository {

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
}