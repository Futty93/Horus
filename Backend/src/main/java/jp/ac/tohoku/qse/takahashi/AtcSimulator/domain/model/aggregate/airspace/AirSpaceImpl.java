package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.Position;

public class AirSpaceImpl implements AirSpace {

    private Map<String, Aircraft> aircrafts; // 空域内の航空機を追跡するためのマップ

    public AirSpaceImpl() {
        this.aircrafts = new HashMap<>();
    }

    @Override
    public void addAircraft(Aircraft aircraft) {
        this.aircrafts.put(aircraft.getCallsign().getValue(), aircraft);
    }

    @Override
    public void removeAircraft(String callsign) {
        this.aircrafts.remove(callsign);
    }

    @Override
    public Aircraft getAircraft(String callsign) {
        return this.aircrafts.get(callsign);
    }

    @Override
    public Collection<Aircraft> getAllAircrafts() {
        return this.aircrafts.values();
    }

    @Override
    public void updateAircraftPosition(String callsign, AircraftPosition newPosition) {
        Aircraft aircraft = this.aircrafts.get(callsign);
        if (aircraft != null) {
            aircraft.setPosition(newPosition);
        }
    }

    @Override
    public int getAircraftCount() {
        return this.aircrafts.size();
    }

    /**
     * 空域内の航空機が重複しているかをチェックするメソッド
     * @param position チェックする位置情報
     * @return 重複している場合は true、そうでない場合は false
     */
    public boolean isAircraftOverlapping(AircraftPosition position) {
        for (Aircraft aircraft : this.aircrafts.values()) {
            if (aircraft.getPosition().equals(position)) {
                return true; // 同じ位置に航空機が存在する場合は重複
            }
        }
        return false;
    }
}