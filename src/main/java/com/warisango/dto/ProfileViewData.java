package com.warisango.dto;

import com.warisango.model.User;

import java.util.List;

public record ProfileViewData(
        User user,
        String memberSince,
        String calculatedTier,
        List<VisitDTO> visits,
        int visitCount,
        int badgeCount
) {
}
