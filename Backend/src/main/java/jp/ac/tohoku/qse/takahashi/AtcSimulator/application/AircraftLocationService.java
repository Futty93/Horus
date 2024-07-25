package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft.Aircraft;

public interface AircraftLocationService {
    String getAircraftLocation(int id);
    String getAllAircraftLocation();
}
