package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * JSON response DTO for aircraft location API.
 * Matches the structure expected by the frontend radar display.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AircraftLocationDto(
    String callsign,
    PositionDto position,
    VectorDto vector,
    InstructedVectorDto instructedVector,
    String type,
    String model,
    String originIata,
    String originIcao,
    String destinationIata,
    String destinationIcao,
    String eta,
    double riskLevel
) {
    public record PositionDto(double latitude, double longitude, double altitude) {}
    public record VectorDto(double heading, double groundSpeed, double verticalSpeed) {}
    public record InstructedVectorDto(double heading, double groundSpeed, double altitude) {}
}
