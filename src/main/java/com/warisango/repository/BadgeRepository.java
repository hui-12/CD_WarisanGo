package com.warisango.repository;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.warisango.model.Badge;
import com.warisango.model.TouristBadge;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
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

    public void saveDefinition(String badgeId, Badge definition) throws Exception {
        firestore.collection(BADGES).document(badgeId).set(toDocument(definition), SetOptions.merge()).get();
    }

    public Badge findDefinition(String badgeId) throws Exception {
        DocumentSnapshot document = firestore.collection(BADGES).document(badgeId).get().get();
        return document.exists() ? toBadge(document) : null;
    }

    public String createDefinition(Badge definition) throws Exception {
        var reference = firestore.collection(BADGES).document();
        Badge stored = new Badge(reference.getId(), definition.name(), definition.emoji(), definition.description(),
                definition.unlockCriteria(), definition.criteriaType(), definition.target());
        reference.set(toDocument(stored)).get();
        return reference.getId();
    }

    public void updateDefinition(String badgeId, Badge definition) throws Exception {
        Badge stored = new Badge(badgeId, definition.name(), definition.emoji(), definition.description(),
                definition.unlockCriteria(), definition.criteriaType(), definition.target());
        firestore.collection(BADGES).document(badgeId).set(toDocument(stored), SetOptions.merge()).get();
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

    public List<Badge> findAllDefinitions() throws Exception {
        List<Badge> definitions = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(BADGES).get().get().getDocuments()) {
            definitions.add(toBadge(document));
        }
        definitions.sort((left, right) -> left.badgeId().compareTo(right.badgeId()));
        return definitions;
    }

    public List<TouristBadge> findEarnedBadges(String touristId) throws Exception {
        List<TouristBadge> earned = new ArrayList<>();
        for (DocumentSnapshot document : firestore.collection(TOURIST_BADGES)
                .whereEqualTo("touristId", touristId).get().get().getDocuments()) {
            String badgeId = document.getString("badgeId");
            var timestamp = document.getTimestamp("dateEarned");
            if (badgeId != null) earned.add(new TouristBadge(document.getId(), badgeId, touristId,
                    timestamp == null ? null : timestamp.toDate().toInstant()));
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

    private Badge toBadge(DocumentSnapshot document) {
        Long target = document.getLong("target");
        return new Badge(document.getId(), document.getString("name"), document.getString("emoji"),
                document.getString("description"), document.getString("unlockCriteria"),
                document.getString("criteriaType"), target == null ? 0 : target.intValue());
    }

    private Map<String, Object> toDocument(Badge badge) {
        Map<String, Object> data = new HashMap<>();
        data.put("badgeId", badge.badgeId());
        data.put("name", badge.name());
        data.put("emoji", badge.emoji());
        data.put("description", badge.description());
        data.put("unlockCriteria", badge.unlockCriteria());
        data.put("criteriaType", badge.criteriaType());
        data.put("target", badge.target());
        return data;
    }
}
