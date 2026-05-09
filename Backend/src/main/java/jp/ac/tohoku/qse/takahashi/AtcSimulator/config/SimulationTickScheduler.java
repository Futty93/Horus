package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals.GlobalVariables;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class SimulationTickScheduler {

    private final AirspaceManagement airspaceManagement;
    private final SimulationTiming simulationTiming;
    private final ThreadPoolTaskScheduler taskScheduler;
    private volatile ScheduledFuture<?> currentFuture;

    public SimulationTickScheduler(
            AirspaceManagement airspaceManagement,
            SimulationTiming simulationTiming,
            ThreadPoolTaskScheduler simulationTaskScheduler) {
        this.airspaceManagement = airspaceManagement;
        this.simulationTiming = simulationTiming;
        this.taskScheduler = simulationTaskScheduler;
    }

    @PostConstruct
    public void start() {
        reschedule();
    }

    @PreDestroy
    public void shutdown() {
        synchronized (this) {
            cancelCurrent();
        }
    }

    public synchronized void reschedule() {
        cancelCurrent();
        long periodMs = simulationTiming.getTickIntervalWallMs();
        currentFuture = taskScheduler.scheduleAtFixedRate(this::tick, Duration.ofMillis(periodMs));
    }

    private void cancelCurrent() {
        ScheduledFuture<?> f = currentFuture;
        if (f != null) {
            f.cancel(false);
            currentFuture = null;
        }
    }

    private void tick() {
        if (!GlobalVariables.isSimulationRunning) {
            return;
        }
        airspaceManagement.nextStep();
    }
}
