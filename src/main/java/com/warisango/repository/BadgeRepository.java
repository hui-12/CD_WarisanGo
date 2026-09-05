package com.warisango.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class BadgeRepository {
    private static final String BADGES = "badges";
    private static final String TOURIST_BADGES = "touristBadges";

    private final Firestore firestore;

    public BadgeRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    public void saveDefinition(String badgeId, Map<String, Object> definition) throws Exception {
        firestore.collection(BADGES).document(badgeId).set(definition, SetOptions.merge()).get();
    }

    public Map<String, Object> findDefinition(String badgeId) throws Exception {
        DocumentSnapshot document = firestore.collection(BADGES).document(badgeId).get().get();
        if (!document.exists()) return null;
        Map<String, Object> definition = new HashMap<>(document.getData());
        definition.putIfAbsent("badgeId", document.getId());
        return definition;
    }

    public String createDefinition(Map<String, Object> definition) throws Exception {
        var reference = firestore.collection(BADGES).document();
        definition.put("badgeId", reference.getId());
        reference.set(definition).get();
        return reference.getId();
    }

    public void updateDefinition(String badgeId, Map<String, Object> definition) throws Exception {
        definition.put("badgeId", badgeId);
        firestore.collection(BADGES).document(badgeId).set(definition, SetOptions.merge()).get();
    }

    public void deleteDefinitionAndAwards(String badgeId) throws Exception {
        for (DocumentSnapshot award : firestore.collection(TOURIST_BADGES)
                .whereEqualTo("badgeId", badgeId).get().get().getDocuments()) {
            award.getReference().delete().get();
        }
        firestore.collection(BADGES).document(badgeId).delete().get();
    }

    public void deleteDefinitionsAndAwards(Set<String> badgeIds) throws Exception {
        for (String badgeId : badgeIds) {
            deleteDefinitionAndAwards(badgeId);
        }
    }

    public List<Map<String, Object>> findAllDefinitions() throws Exception {
        List<Map<String, Object>> definitions = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(BADGES).get().get().getDocuments()) {
            Map<String, Object> data = new HashMap<>(document.getData());
            data.putIfAbsent("badgeId", document.getId());
            definitions.add(data);
        }
        definitions.sort((left, right) -> String.valueOf(left.get("badgeId"))
                .compareTo(String.valueOf(right.get("badgeId"))));
        return definitions;
    }

    public Map<String, Instant> findEarnedBadges(String touristId) throws Exception {
        Map<String, Instant> earned = new LinkedHashMap<>();
        for (DocumentSnapshot document : firestore.collection(TOURIST_BADGES)
                .whereEqualTo("touristId", touristId).get().get().getDocuments()) {
            String badgeId = document.getString("badgeId");
            var timestamp = document.getTimestamp("dateEarned");
            if (badgeId != null) earned.put(badgeId, timestamp == null ? null : timestamp.toDate().toInstant());
        }
        return earned;
    }

    public void awardBadge(String touristId, String badgeId) throws Exception {
        String documentId = touristId + "_" + badgeId;
        Map<String, Object> data = new HashMap<>();
        data.put("badgeId", badgeId);
        data.put("dateEarned", FieldValue.serverTimestamp());
        data.put("touristId", touristId);
        firestore.collection(TOURIST_BADGES).document(documentId).set(data).get();
    }

    public void revokeBadge(String touristId, String badgeId) throws Exception {
        String documentId = touristId + "_" + badgeId;
        firestore.collection(TOURIST_BADGES).document(documentId).delete().get();
    }
}
