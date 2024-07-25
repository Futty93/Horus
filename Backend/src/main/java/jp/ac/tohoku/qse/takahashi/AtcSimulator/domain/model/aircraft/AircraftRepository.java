package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft;

import java.net.HttpRetryException;
import java.util.List;

public interface AircraftRepository {
    Aircraft find(int id);

    List<Aircraft> findAll();
}
