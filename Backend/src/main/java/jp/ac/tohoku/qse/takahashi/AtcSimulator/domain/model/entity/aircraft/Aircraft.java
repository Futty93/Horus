package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public interface Aircraft {
    void calculateNextAircraftPosition();

    void calculateNextAircraftVector();

    double calculateTurnAngle(FixPosition fixPosition);

    boolean isEqualCallsign(Callsign callsign);

    Callsign getCallsign();

    AircraftPosition getAircraftPosition();

    AircraftVector getAircraftVector();

    InstructedVector getInstructedVector();

    AircraftType getAircraftType();

    /** Returns true when the aircraft has passed a REMOVE_AIRCRAFT waypoint and should be removed from simulation. */
    default boolean shouldBeRemovedFromSimulation() {
        return false;
    }
}
