package com.warisango.model.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class ChallengeService {
    private static final String CHALLENGES = "Challenges";
    private static final String USER_CHALLENGES = "UserChallenges";
    private static final String CHECKINS = "CheckIns";
    private static final String USERS = "users";
    private static final String HISTORY = "PointHistory";

    public List<Map<String,Object>> getChallenges(String userId) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snapshot = db.collection(CHALLENGES).get().get();
        List<Map<String,Object>> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Map<String,Object> c = toMap(doc);
            String id = doc.getId();
            int total = intValue(c.get("target"), 1);
            int progress = (int) db.collection(CHECKINS)
                    .whereEqualTo("userId", userId).get().get().getDocuments().stream()
                    .filter(check -> isEligibleCheckin(check, c))
                    .count();
            DocumentSnapshot uc = db.collection(USER_CHALLENGES).document(userId + "_" + id).get().get();
            boolean joined = uc.exists() && Boolean.TRUE.equals(uc.getBoolean("joined"));
            boolean completed = uc.exists() && Boolean.TRUE.equals(uc.getBoolean("completed"));
            c.put("id", id);
            c.put("target", total);
            c.put("progress", Math.min(progress, total));
            c.put("joined", joined);
            c.put("done", completed || progress >= total && joined);
            result.add(c);
        }
        return result;
    }

    private boolean isEligibleCheckin(DocumentSnapshot checkin, Map<String,Object> challenge) {
        String businessId = checkin.getString("businessId");
        String requirement = String.valueOf(challenge.getOrDefault("requirement", "")).toLowerCase();
        if (requirement.contains("penang")) {
            // For location-specific challenges, the business state is stored on the check-in when available.
            String state = checkin.getString("businessState");
            return state == null || state.toLowerCase().contains("penang");
        }
        return businessId != null;
    }

    public void join(String userId, String challengeId) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(USER_CHALLENGES).document(userId + "_" + challengeId);
        Map<String,Object> data = new HashMap<>();
        data.put("userId", userId); data.put("challengeId", challengeId); data.put("joined", true);
        data.put("completed", false); data.put("joinedAt", FieldValue.serverTimestamp());
        ref.set(data, SetOptions.merge()).get();
    }

    public Map<String,Object> claim(String userId, String challengeId) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot challenge = db.collection(CHALLENGES).document(challengeId).get().get();
        if (!challenge.exists()) throw new IllegalArgumentException("Challenge not found.");
        int target = intValue(challenge.get("target"), 1);
        int progress = db.collection(CHECKINS).whereEqualTo("userId", userId).get().get().getDocuments().size();
        DocumentReference ucRef = db.collection(USER_CHALLENGES).document(userId + "_" + challengeId);
        DocumentReference userRef = db.collection(USERS).document(userId);
        int reward = intValue(challenge.get("rewardPoints"), 0);

        int newPoints = db.runTransaction(tx -> {
            DocumentSnapshot uc = tx.get(ucRef).get();
            if (!uc.exists() || !Boolean.TRUE.equals(uc.getBoolean("joined"))) throw new IllegalStateException("Join the challenge first.");
            if (Boolean.TRUE.equals(uc.getBoolean("completed"))) throw new IllegalStateException("Challenge already completed.");
            if (progress < target) throw new IllegalStateException("Challenge requirements are not completed.");
            DocumentSnapshot user = tx.get(userRef).get();
            int current = user.exists() && user.getLong("totalPoints") != null
                    ? user.getLong("totalPoints").intValue() : 0;
            int updated = current + reward;
            Map<String,Object> u = new HashMap<>(); u.put("userId", userId); u.put("totalPoints", updated);
            tx.set(userRef, u, SetOptions.merge());
            tx.set(ucRef, Map.of("completed", true, "completedAt", FieldValue.serverTimestamp()), SetOptions.merge());
            DocumentReference h = db.collection(HISTORY).document();
            Map<String,Object> history = new HashMap<>();
            history.put("userId", userId); history.put("type", "CHALLENGE");
            history.put("description", "Challenge: " + challenge.getString("title"));
            history.put("points", reward); history.put("referenceId", challengeId); history.put("timestamp", FieldValue.serverTimestamp());
            tx.set(h, history);
            return updated;
        }).get();
        return Map.of("success", true, "pointsEarned", reward, "currentPoints", newPoints);
    }

    public List<Map<String,Object>> adminList() throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snap = db.collection(CHALLENGES).get().get();
        List<Map<String,Object>> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap.getDocuments()) list.add(toMapWithId(doc));
        return list;
    }

    public String create(Map<String,Object> input) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Map<String,Object> data = normalize(input);
        DocumentReference ref = db.collection(CHALLENGES).document();
        ref.set(data).get();
        return ref.getId();
    }

    public void update(String id, Map<String,Object> input) throws Exception {
        FirestoreClient.getFirestore().collection(CHALLENGES).document(id).set(normalize(input), SetOptions.merge()).get();
    }

    public void delete(String id) throws Exception {
        FirestoreClient.getFirestore().collection(CHALLENGES).document(id).delete().get();
    }

    private Map<String,Object> normalize(Map<String,Object> input) {
        Map<String,Object> d = new HashMap<>();
        d.put("title", input.getOrDefault("title", "Untitled Challenge"));
        d.put("description", input.getOrDefault("description", ""));
        d.put("requirement", input.getOrDefault("requirement", ""));
        d.put("target", intValue(input.get("target"), 1));
        d.put("rewardPoints", intValue(input.get("rewardPoints"), 0));
        d.put("badge", input.getOrDefault("badge", ""));
        d.put("expiry", input.getOrDefault("expiry", ""));
        d.put("status", input.getOrDefault("status", "ACTIVE"));
        return d;
    }

    private Map<String,Object> toMap(DocumentSnapshot doc) { return new HashMap<>(doc.getData() == null ? Collections.emptyMap() : doc.getData()); }
    private Map<String,Object> toMapWithId(DocumentSnapshot doc) { Map<String,Object> m=toMap(doc); m.put("id",doc.getId()); return m; }
    private int intValue(Object v, int fallback) { if (v instanceof Number n) return n.intValue(); try { return v==null?fallback:Integer.parseInt(String.valueOf(v)); } catch(Exception e){return fallback;} }
}
