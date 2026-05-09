package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

/**
 * DTO for conflict API health endpoint response.
 */
public record HealthStatusDto(
    String status,
    long timestamp,
    long totalConflicts,
    long criticalConflicts
) {}
