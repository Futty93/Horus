package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SpawnWithFlightPlanDto(
        @Valid @NotNull FlightPlanDto flightPlan,
        @Valid InitialPositionDto initialPosition
) {
}
