package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

/**
 * DTO for conflict alert API response.
 * Flattened from RiskAssessment to avoid domain leakage at API boundary.
 */
public record ConflictAlertDto(
    String pairId,
    double riskLevel,
    String alertLevel,
    double timeToClosest,
    double closestHorizontalDistance,
    double closestVerticalDistance,
    boolean conflictPredicted
) {}
