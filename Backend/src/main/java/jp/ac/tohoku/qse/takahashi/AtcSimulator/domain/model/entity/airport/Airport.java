package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.airport;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.Runway;

import java.util.List;

public class Airport {
    private final String name;
    private final String icaoCode;
    private final String iataCode;
    private final Latitude latitude;
    private final Longitude longitude;
    private final Altitude elevation;
    private final List<Runway> runway;

    public Airport(String name, String icaoCode, String iataCode, Latitude latitude, Longitude longitude, Altitude elevation, List<Runway> runway) {
        this.name = name;
        this.icaoCode = icaoCode;
        this.iataCode = iataCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.runway = runway;
    }
}
