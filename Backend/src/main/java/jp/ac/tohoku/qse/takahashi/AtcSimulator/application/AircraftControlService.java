package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;

public interface AircraftControlService {
    void changeAltitude(Aircraft aircraft, int newAltitude);
    void changeHeading(Aircraft aircraft, int newHeading);
    void changeSpeed(Aircraft aircraft, int newSpeed);
    void updatePosition(Aircraft aircraft);
}