package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

/**
 * DTO for conflict alert statistics API response.
 * No domain dependencies.
 */
public record ConflictStatisticsDto(
    long totalConflicts,
    long safeCount,
    long whiteConflictCount,
    long redConflictCount,
    long separationViolationCount,
    double maxRiskLevel,
    double avgRiskLevel
) {}
