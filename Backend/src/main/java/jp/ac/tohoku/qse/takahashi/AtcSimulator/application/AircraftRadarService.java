package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public interface AircraftRadarService {
    String getAircraftLocation(Callsign callsign);
    String getAllAircraftLocation();

}
