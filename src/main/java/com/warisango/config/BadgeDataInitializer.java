package com.warisango.config;

import com.warisango.model.repository.BadgeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
public class BadgeDataInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(BadgeDataInitializer.class);
    private final BadgeRepository badgeRepository;

    public BadgeDataInitializer(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        try {
            badgeRepository.deleteDefinitionsAndAwards(Set.of(
                    "badge_005", "badge_006", "badge_007", "badge_008"));
            for (Map<String, Object> badge : badgeDefinitions()) {
                String badgeId = String.valueOf(badge.get("badgeId"));
                Map<String, Object> existing = badgeRepository.findDefinition(badgeId);
                if (isOldDefinition(existing, badge)) {
                    badgeRepository.deleteDefinitionAndAwards(badgeId);
                }
                badgeRepository.saveDefinition(badgeId, badge);
            }
            logger.info("Four check-in badge definitions synchronized with Firebase.");
        } catch (Exception exception) {
            logger.error("Badge initialization failed; application startup will continue.", exception);
        }
    }

    private List<Map<String, Object>> badgeDefinitions() {
        return List.of(
                badge("badge_001", "First Journey", "\uD83D\uDC63",
                        "Complete your first heritage business check-in.", 1),
                badge("badge_002", "Heritage Starter", "\uD83E\uDD62",
                        "Check in at three different heritage food businesses.", 3),
                badge("badge_003", "Food Explorer", "\uD83E\uDDED",
                        "Check in at five different heritage food businesses.", 5),
                badge("badge_004", "Heritage Hunter", "\uD83D\uDDFA\uFE0F",
                        "Check in at ten different heritage food businesses.", 10)
        );
    }

    private Map<String, Object> badge(String id, String name, String emoji,
                                      String description, int target) {
        return Map.of(
                "badgeId", id,
                "name", name,
                "emoji", emoji,
                "description", description,
                "unlockCriteria", "Check in at " + target + (target == 1 ? " business" : " businesses"),
                "criteriaType", "checkInCount",
                "target", target
        );
    }

    private boolean isOldDefinition(Map<String, Object> existing, Map<String, Object> expected) {
        if (existing == null) return false;
        return !expected.get("name").equals(existing.get("name"))
                || !expected.get("criteriaType").equals(existing.get("criteriaType"))
                || !(existing.get("target") instanceof Number target)
                || target.intValue() != ((Number) expected.get("target")).intValue();
    }
}
