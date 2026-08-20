package com.warisango.dto;

/**
 * Returns the current user's like state and the total like count for one item.
 */
public record LikeStatusDTO(boolean liked, int likeCount) {
}
