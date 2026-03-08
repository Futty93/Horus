package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScenarioLoadDto(
        String scenarioName,
        String description,
        String createdAt,
        @Valid @NotNull List<SpawnWithFlightPlanDto> aircraft
) {
}
