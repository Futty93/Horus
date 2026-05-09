package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.HoldTurnDirection;

import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HoldRequestDto(
        @NotBlank String fixName,
        String turnDirection
) {
    public HoldTurnDirection resolvedTurnDirection() {
        if (turnDirection == null || turnDirection.isBlank()) {
            return HoldTurnDirection.RIGHT;
        }
        return HoldTurnDirection.valueOf(turnDirection.trim().toUpperCase());
    }
}
