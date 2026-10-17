package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record TowerHandoffRequestDto(
        @NotBlank String airportIcao,
        @NotBlank String runwayDesignator) {
}
