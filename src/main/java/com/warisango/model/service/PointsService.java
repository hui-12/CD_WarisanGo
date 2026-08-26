package com.warisango.model.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class PointsService {
    private static final String USERS = "users";
    private static final String HISTORY = "PointHistory";

    public int getCurrentPoints(String userId) throws ExecutionException, InterruptedException {
        DocumentReference ref = FirestoreClient.getFirestore().collection(USERS).document(userId);
        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) {
            Map<String,Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("totalPoints", 0L);
            ref.set(data).get();
            return 0;
        }
        Long points = snap.getLong("totalPoints");
        return points == null ? 0 : points.intValue();
    }

    public int addPoints(String userId, int points, String type, String description, String referenceId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference userRef = db.collection(USERS).document(userId);

        int newPoints = db.runTransaction(tx -> {
            DocumentSnapshot user = tx.get(userRef).get();
            int current = user.exists() && user.getLong("totalPoints") != null
                    ? user.getLong("totalPoints").intValue() : 0;
            int updated = current + points;
            Map<String,Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("totalPoints", updated);
            tx.set(userRef, userData, SetOptions.merge());

            DocumentReference historyRef = db.collection(HISTORY).document();
            Map<String,Object> history = new HashMap<>();
            history.put("userId", userId);
            history.put("type", type);
            history.put("description", description);
            history.put("points", points);
            history.put("referenceId", referenceId);
            history.put("timestamp", FieldValue.serverTimestamp());
            tx.set(historyRef, history);
            return updated;
        }).get();
        return newPoints;
    }

    public List<Map<String,Object>> getHistory(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snap = db.collection(HISTORY)
                .whereEqualTo("userId", userId)
                .get().get();
        List<Map<String,Object>> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap.getDocuments()) {
            Map<String,Object> row = new HashMap<>();
            row.put("id", doc.getId());
            row.put("type", doc.getString("type"));
            row.put("description", doc.getString("description"));
            Long p = doc.getLong("points");
            row.put("points", p == null ? 0 : p);
            row.put("timestamp", doc.getTimestamp("timestamp") == null ? null : doc.getTimestamp("timestamp").toDate().toString());
            result.add(row);
        }
        result.sort((a,b) -> String.valueOf(b.get("timestamp")).compareTo(String.valueOf(a.get("timestamp"))));
        return result;
    }

    public List<Map<String,Object>> getLeaderboard() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snap = db.collection(USERS).get().get();
        List<Map<String,Object>> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap.getDocuments()) {
            Long p = doc.getLong("totalPoints");
            if (p == null) continue;
            Map<String,Object> row = new HashMap<>();
            row.put("userId", doc.getId());
            row.put("name", doc.getString("name") == null ? doc.getId() : doc.getString("name"));
            row.put("points", p);
            result.add(row);
        }
        result.sort((a,b) -> Long.compare(((Number)b.get("points")).longValue(), ((Number)a.get("points")).longValue()));
        for (int i=0; i<result.size(); i++) result.get(i).put("rank", i+1);
        return result;
    }
}
