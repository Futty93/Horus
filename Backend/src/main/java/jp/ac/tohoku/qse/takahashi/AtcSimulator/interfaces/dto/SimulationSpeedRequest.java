package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public record SimulationSpeedRequest(
        @NotNull(message = "speedMultiplier is required") Double speedMultiplier) {}
