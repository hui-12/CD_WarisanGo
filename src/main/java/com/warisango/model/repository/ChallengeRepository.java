package com.warisango.model.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.warisango.util.TierCalculator;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ChallengeRepository {
    private static final String CHALLENGES = "challenges";
    private static final String PARTICIPATIONS = "challengeParticipations";
    private static final String CHECK_INS = "checkIns";
    private static final String USERS = "users";
    private static final String POINTS_HISTORIES = "pointsHistories";

    private final Firestore firestore;

    public ChallengeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public List<Map<String, Object>> findAllChallenges() throws Exception {
        List<Map<String, Object>> challenges = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(CHALLENGES).get().get().getDocuments()) {
            Map<String, Object> challenge = dataWithId(document);
            challenges.add(challenge);
        }
        return challenges;
    }

    public Map<String, Object> findChallenge(String challengeId) throws Exception {
        DocumentSnapshot document = firestore.collection(CHALLENGES).document(challengeId).get().get();
        return document.exists() ? dataWithId(document) : null;
    }

    public List<Map<String, Object>> findCheckIns(String touristId) throws Exception {
        return firestore.collection(CHECK_INS)
                .whereEqualTo("touristId", touristId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .map(document -> copyData(document.getData()))
                .toList();
    }

    public Map<String, Object> findParticipation(String touristId, String challengeId) throws Exception {
        return firestore.collection(PARTICIPATIONS)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .filter(document -> touristId.equals(document.getString("touristId")))
                .findFirst()
                .map(this::dataWithId)
                .orElse(null);
    }

    public void saveParticipation(String participationId, Map<String, Object> data) throws Exception {
        DocumentReference reference = participationId == null
                ? firestore.collection(PARTICIPATIONS).document()
                : firestore.collection(PARTICIPATIONS).document(participationId);
        reference.set(data, SetOptions.merge()).get();
    }

    public void updateParticipationProgress(String participationId, String progress) throws Exception {
        firestore.collection(PARTICIPATIONS).document(participationId).update("progress", progress).get();
    }

    public int claimReward(String touristId, String challengeId, String participationId,
                           int rewardPoints, int target) throws Exception {
        DocumentReference participation = firestore.collection(PARTICIPATIONS).document(participationId);
        DocumentReference user = firestore.collection(USERS).document(touristId);
        DocumentReference history = firestore.collection(POINTS_HISTORIES).document();

        return firestore.runTransaction(transaction -> {
            DocumentSnapshot currentParticipation = transaction.get(participation).get();
            if (!currentParticipation.exists()) {
                throw new IllegalStateException("Challenge participation was not found.");
            }
            if ("completed".equalsIgnoreCase(currentParticipation.getString("status"))) {
                throw new IllegalStateException("Challenge reward has already been claimed.");
            }

            DocumentSnapshot currentUser = transaction.get(user).get();
            Long storedPoints = currentUser.exists() ? currentUser.getLong("totalPoints") : null;
            int updatedPoints = (storedPoints == null ? 0 : storedPoints.intValue()) + rewardPoints;

            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("totalPoints", updatedPoints);
            userUpdate.put("tierStatus", TierCalculator.tierFor(updatedPoints));
            transaction.set(user, userUpdate, SetOptions.merge());

            Map<String, Object> participationUpdate = new HashMap<>();
            participationUpdate.put("status", "completed");
            participationUpdate.put("progress", target + "/" + target);
            participationUpdate.put("completedDate", FieldValue.serverTimestamp());
            transaction.set(participation, participationUpdate, SetOptions.merge());

            Map<String, Object> historyData = new HashMap<>();
            historyData.put("activityDate", FieldValue.serverTimestamp());
            historyData.put("activityType", "challenge");
            historyData.put("pointsEarned", rewardPoints);
            historyData.put("relatedChallengeId", challengeId);
            historyData.put("touristId", touristId);
            historyData.put("transactionId", history.getId());
            transaction.set(history, historyData);
            return updatedPoints;
        }).get();
    }

    public String createChallenge(Map<String, Object> data) throws Exception {
        DocumentReference reference = firestore.collection(CHALLENGES).document();
        reference.set(data).get();
        return reference.getId();
    }

    public void updateChallenge(String id, Map<String, Object> data) throws Exception {
        firestore.collection(CHALLENGES).document(id).set(data, SetOptions.merge()).get();
    }

    public void deleteChallenge(String id) throws Exception {
        firestore.collection(CHALLENGES).document(id).delete().get();
    }

    public int countCompletedParticipations(String touristId) throws Exception {
        return (int) firestore.collection(PARTICIPATIONS)
                .whereEqualTo("touristId", touristId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .filter(document -> "completed".equalsIgnoreCase(document.getString("status")))
                .count();
    }

    private Map<String, Object> dataWithId(DocumentSnapshot document) {
        Map<String, Object> data = new HashMap<>();
        if (document.getData() != null) data.putAll(document.getData());
        data.put("id", document.getId());
        return data;
    }

    private Map<String, Object> copyData(Map<String, Object> source) {
        return new HashMap<>(source);
    }
}
