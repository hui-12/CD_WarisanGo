package com.warisango.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.warisango.exception.OperationConflictException;
import com.warisango.model.Challenge;
import com.warisango.model.ChallengeParticipation;
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

    public List<Challenge> findAllChallenges() throws Exception {
        List<Challenge> challenges = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(CHALLENGES).get().get().getDocuments()) {
            challenges.add(toChallenge(document));
        }
        return challenges;
    }

    public Challenge findChallenge(String challengeId) throws Exception {
        DocumentSnapshot document = firestore.collection(CHALLENGES).document(challengeId).get().get();
        return document.exists() ? toChallenge(document) : null;
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

    public ChallengeParticipation findParticipation(String touristId, String challengeId) throws Exception {
        return firestore.collection(PARTICIPATIONS)
                .whereEqualTo("challengeId", challengeId)
                .get()
                .get()
                .getDocuments()
                .stream()
                .filter(document -> touristId.equals(document.getString("touristId")))
                .findFirst()
                .map(this::toParticipation)
                .orElse(null);
    }

    public void saveParticipation(String participationId, ChallengeParticipation participation) throws Exception {
        DocumentReference reference = participationId == null
                ? firestore.collection(PARTICIPATIONS).document()
                : firestore.collection(PARTICIPATIONS).document(participationId);
        reference.set(toDocument(participation), SetOptions.merge()).get();
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
                throw new OperationConflictException("Challenge participation was not found.");
            }
            if ("completed".equalsIgnoreCase(currentParticipation.getString("status"))) {
                throw new OperationConflictException("Challenge reward has already been claimed.");
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

    public String createChallenge(Challenge data) throws Exception {
        DocumentReference reference = firestore.collection(CHALLENGES).document();
        Map<String, Object> challenge = toDocument(data);
        challenge.put("createdDate", FieldValue.serverTimestamp());
        reference.set(challenge).get();
        return reference.getId();
    }

    public void updateChallenge(String id, Challenge data) throws Exception {
        firestore.collection(CHALLENGES).document(id).set(toDocument(data), SetOptions.merge()).get();
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

    private Challenge toChallenge(DocumentSnapshot document) {
        Long target = document.getLong("target");
        Long rewardPoints = document.getLong("rewardPoints");
        return new Challenge(document.getId(), document.getString("title"), document.getString("description"),
                document.getString("requirement"), target == null ? 0 : target.intValue(),
                rewardPoints == null ? 0 : rewardPoints.intValue(), document.getString("badge"),
                document.getString("expiry"), document.getString("status"), toInstant(document, "createdDate"));
    }

    private ChallengeParticipation toParticipation(DocumentSnapshot document) {
        return new ChallengeParticipation(document.getId(), document.getString("touristId"),
                document.getString("challengeId"), document.getString("progress"), document.getString("status"),
                toInstant(document, "completedDate"));
    }

    private Map<String, Object> toDocument(Challenge challenge) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", challenge.title());
        data.put("description", challenge.description());
        data.put("requirement", challenge.requirement());
        data.put("target", challenge.target());
        data.put("rewardPoints", challenge.rewardPoints());
        data.put("badge", challenge.badge());
        data.put("expiry", challenge.expiry());
        data.put("status", challenge.status());
        return data;
    }

    private Map<String, Object> toDocument(ChallengeParticipation participation) {
        Map<String, Object> data = new HashMap<>();
        data.put("touristId", participation.touristId());
        data.put("challengeId", participation.challengeId());
        data.put("progress", participation.progress());
        data.put("status", participation.status());
        data.put("completedDate", participation.completedDate());
        return data;
    }

    private java.time.Instant toInstant(DocumentSnapshot document, String field) {
        var timestamp = document.getTimestamp(field);
        return timestamp == null ? null : timestamp.toDate().toInstant();
    }

    private Map<String, Object> copyData(Map<String, Object> source) {
        return new HashMap<>(source);
    }
}
