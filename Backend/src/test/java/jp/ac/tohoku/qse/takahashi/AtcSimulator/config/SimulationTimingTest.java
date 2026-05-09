package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SimulationTimingTest {

    @Test
    void isValidPreset_acceptsDiscreteValues() {
        assertThat(SimulationTiming.isValidPreset(0.25)).isTrue();
        assertThat(SimulationTiming.isValidPreset(0.5)).isTrue();
        assertThat(SimulationTiming.isValidPreset(1.0)).isTrue();
        assertThat(SimulationTiming.isValidPreset(2.0)).isTrue();
        assertThat(SimulationTiming.isValidPreset(4.0)).isTrue();
        assertThat(SimulationTiming.isValidPreset(10.0)).isTrue();
    }

    @Test
    void isValidPreset_rejectsOtherValues() {
        assertThat(SimulationTiming.isValidPreset(3.0)).isFalse();
        assertThat(SimulationTiming.isValidPreset(0.1)).isFalse();
        assertThat(SimulationTiming.isValidPreset(Double.NaN)).isFalse();
    }

    @Test
    void tickIntervalWallMs_followsMultiplier() {
        SimulationTiming timing = new SimulationTiming();
        timing.setSpeedMultiplier(1.0);
        assertThat(timing.getTickIntervalWallMs()).isEqualTo(1000);
        timing.setSpeedMultiplier(2.0);
        assertThat(timing.getTickIntervalWallMs()).isEqualTo(500);
        timing.setSpeedMultiplier(10.0);
        assertThat(timing.getTickIntervalWallMs()).isEqualTo(100);
        timing.setSpeedMultiplier(0.25);
        assertThat(timing.getTickIntervalWallMs()).isEqualTo(4000);
    }

    @Test
    void setSpeedMultiplier_rejectsInvalid() {
        SimulationTiming timing = new SimulationTiming();
        assertThatThrownBy(() -> timing.setSpeedMultiplier(8.0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
