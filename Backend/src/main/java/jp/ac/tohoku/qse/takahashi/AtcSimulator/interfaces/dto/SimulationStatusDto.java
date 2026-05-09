package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

public record SimulationStatusDto(
        boolean isSimulationRunning,
        double speedMultiplier,
        int tickIntervalWallMs
) {}
