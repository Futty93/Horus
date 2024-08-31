package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.airport;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.Runway;

import java.util.List;

public class Airport {
    private final String name;
    private final String icaoCode;
    private final String iataCode;
    private final double latitude;
    private final double longitude;
    private final int elevation;
    private final List<Runway> runway;

    public Airport(String name, String icaoCode, String iataCode, double latitude, double longitude, int elevation, List<Runway> runway) {
        this.name = name;
        this.icaoCode = icaoCode;
        this.iataCode = iataCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.runway = runway;
    }
}
