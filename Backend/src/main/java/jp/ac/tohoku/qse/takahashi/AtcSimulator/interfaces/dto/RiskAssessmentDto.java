package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

/**
 * DTO for conflict risk assessment in map responses (e.g. /api/conflict/all).
 * Flattened from RiskAssessment to avoid domain leakage at API boundary.
 * pairId is the map key, so omitted here.
 */
public record RiskAssessmentDto(
    double riskLevel,
    String alertLevel,
    double timeToClosest,
    double closestHorizontalDistance,
    double closestVerticalDistance,
    boolean conflictPredicted
) {}
