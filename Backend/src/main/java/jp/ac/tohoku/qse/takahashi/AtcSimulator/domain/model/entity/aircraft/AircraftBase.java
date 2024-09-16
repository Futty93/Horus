package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public abstract class AircraftBase {
    final Callsign callsign;
    AircraftPosition aircraftPosition;
    AircraftVector aircraftVector;
    InstructedVector instructedVector;
    final AircraftType aircraftType;

    public AircraftBase(Callsign callsign, AircraftType aircraftType, AircraftPosition aircraftPosition, AircraftVector aircraftVector) {
        this.callsign = callsign;
        this.aircraftType = aircraftType;
        this.aircraftPosition = aircraftPosition;
        this.aircraftVector = aircraftVector;
        this.instructedVector = new InstructedVector(aircraftVector.heading, aircraftPosition.altitude, aircraftVector.groundSpeed);
    }

    // Getter
    public Callsign getCallsign() {
        return this.callsign;
    }

    public AircraftPosition getAircraftPosition() {
        return this.aircraftPosition;
    }

    public AircraftVector getAircraftVector() {
        return this.aircraftVector;
    }

    public InstructedVector getInstructedVector() {
        return this.instructedVector;
    }

    public AircraftType getAircraftType() {
        return this.aircraftType;
    }

    // Setter
    public void setAircraftPosition(final AircraftPosition newAircraftPosition) {
        this.aircraftPosition = newAircraftPosition;
    }

    public void setAircraftVector(final AircraftVector newAircraftVector) {
        this.aircraftVector = newAircraftVector;
    }

    public void setInstructedVector(final InstructedVector newInstructedVector) {
        this.instructedVector = newInstructedVector;
    }

    // abstract methods
    public boolean isEqualCallsign(Callsign callsign) {
        return this.callsign.equals(callsign);
    }
}
