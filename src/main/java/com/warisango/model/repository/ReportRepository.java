package com.warisango.model.repository;

import com.warisango.dto.ReportDTO;

import java.util.List;

/**
 * Repository contract for the root-level reports collection.
 */
public interface ReportRepository {

    List<ReportDTO> findAll();

    ReportDTO findByReportId(String reportId);

    boolean existsByReporterAndTarget(
            String reporterTouristId,
            String targetType,
            String targetId
    );

    void save(ReportDTO report);

    void update(ReportDTO report);

    String generateNextReportId();
}
