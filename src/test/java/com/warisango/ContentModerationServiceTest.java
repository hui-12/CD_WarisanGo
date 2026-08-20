package com.warisango;

import com.warisango.model.service.ContentModerationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentModerationServiceTest {

    private final ContentModerationService moderationService = new ContentModerationService();

    @Test
    void blocksEnglishMalayAndChineseTerms() {
        assertTrue(moderationService.containsProhibitedContent("This is fucking bad."));
        assertTrue(moderationService.containsProhibitedContent("Tempat ini sangat bodoh."));
        assertTrue(moderationService.containsProhibitedContent("这个评论包含傻逼内容。"));
        assertTrue(moderationService.containsProhibitedContent("That person is a dumbass."));
        assertTrue(moderationService.containsProhibitedContent("Dia memang bangsat."));
        assertTrue(moderationService.containsProhibitedContent("你这个脑残。"));
    }

    @Test
    void blocksTermsSeparatedByPunctuation() {
        assertTrue(moderationService.containsProhibitedContent("f.u.c.k"));
        assertTrue(moderationService.containsProhibitedContent("son.of.a.bitch"));
    }

    @Test
    void allowsNormalReviewText() {
        assertFalse(moderationService.containsProhibitedContent(
                "The laksa was delicious and the staff were friendly."));
        assertFalse(moderationService.containsProhibitedContent(
                "Makanan tradisional ini sedap dan perkhidmatannya mesra."));
        assertFalse(moderationService.containsProhibitedContent(
                "这家店的传统美食很好吃，服务也很友善。"));
    }

    @Test
    void doesNotMatchShortProfanityInsideNormalWords() {
        assertFalse(moderationService.containsProhibitedContent(
                "The class had excellent service and a classic menu."));
    }
}
