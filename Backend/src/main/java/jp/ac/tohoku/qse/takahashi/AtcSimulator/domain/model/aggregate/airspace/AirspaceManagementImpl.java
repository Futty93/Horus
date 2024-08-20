package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.valueObject.Position;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// TODO: Airspaceを複数にする
public class AirspaceManagementImpl implements AirspaceManagement {

    private final Map<String, Aircraft> aircraftsInAirspace = new HashMap<>();
    private final AirSpace airSpace;

    public AirspaceManagementImpl(AirSpace airSpace) {
        this.airSpace = airSpace;
    }

    @Override
    public void addAircraftToAirspace(Aircraft aircraft) {
        aircraftsInAirspace.put(aircraft.getCallsign(), aircraft);
    }

    @Override
    public void removeAircraftFromAirspace(String callsign) {
        aircraftsInAirspace.remove(callsign);
    }

    @Override
    public Collection<Aircraft> getAllAircraftsInAirspace() {
        return aircraftsInAirspace.values();
    }

    @Override
    public void updateAircraftPositionInAirspace(String callsign, Position newPosition) {
        Aircraft aircraft = aircraftsInAirspace.get(callsign);
        if (aircraft != null) {
            aircraft.setPosition(newPosition);
        }
    }

    @Override
    public int getAircraftCountInAirspace() {
        return aircraftsInAirspace.size();
    }

    @Override
    public boolean isAircraftOverlappingInAirspace(Position position) {
        for (Aircraft aircraft : aircraftsInAirspace.values()) {
            if (aircraft.getPosition().equals(position)) {
                return true;
            }
        }
        return false;
    }
}