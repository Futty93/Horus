package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Aircraft spawn request with flight plan and initial position.
 *
 * @param flightPlan      Flight plan definition
 * @param initialPosition Initial position (required for immediate spawn)
 * @param spawnTime       Seconds from simulation start to spawn. null or 0 = immediate. Reserved for future delayed spawn.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpawnWithFlightPlanDto(
        @Valid @NotNull FlightPlanDto flightPlan,
        @Valid InitialPositionDto initialPosition,
        Integer spawnTime
) {
}
