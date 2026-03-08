package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FlightPlanWaypointDto(
        @NotBlank String fix,
        Integer altitude,
        String constraint,
        Integer speed,
        String action
) {
}
