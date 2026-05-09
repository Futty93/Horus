package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Runtime simulation speed (wall-clock multiplier). Single source for tick interval and API exposure.
 * One simulation tick always advances domain time by {@value SIM_DELTA_SECONDS} seconds; multiplier only
 * changes how often ticks run in real time.
 */
@Component
public class SimulationTiming {

    public static final double SIM_DELTA_SECONDS = 1.0;

    private static final double EPS = 1e-6;
    private static final Set<Double> PRESETS = Set.of(0.25, 0.5, 1.0, 2.0, 4.0, 10.0);

    private final Object lock = new Object();
    private double speedMultiplier = 1.0;

    public static boolean isValidPreset(double multiplier) {
        for (double p : PRESETS) {
            if (Math.abs(p - multiplier) < EPS) {
                return true;
            }
        }
        return false;
    }

    public double getSpeedMultiplier() {
        synchronized (lock) {
            return speedMultiplier;
        }
    }

    public int getTickIntervalWallMs() {
        synchronized (lock) {
            return (int) Math.max(1L, Math.round(1000.0 / speedMultiplier));
        }
    }

    public void setSpeedMultiplier(double multiplier) {
        if (!isValidPreset(multiplier)) {
            throw new IllegalArgumentException("speedMultiplier must be one of: 0.25, 0.5, 1, 2, 4, 10");
        }
        synchronized (lock) {
            this.speedMultiplier = multiplier;
        }
    }
}
