package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public class CommercialAircraft implements Aircraft {
    private final Callsign callsign;
    private AircraftPosition aircraftPosition;
    private AircraftVector aircraftVector;
    private InstructedVector instructedVector;
    private final AircraftType aircraftType;
    private final String originIata;
    private final String originIcao;
    private final String destinationIata;
    private final String destinationIcao;
    private final String eta; //Estimated Time of Arrival in ISO 8601 format

    //Constructor
    public CommercialAircraft(Callsign callsign, AircraftPosition aircraftPosition, AircraftVector aircraftVector, AircraftType aircraftType, String originIata, String originIcao, String destinationIata, String destinationIcao, String eta) {
        this.callsign = callsign;
        this.aircraftPosition = aircraftPosition;
        this.aircraftVector = aircraftVector;
        this.instructedVector = new InstructedVector(aircraftVector.getHeading(), (int)(aircraftPosition.getAltitude()), aircraftVector.getGroundSpeed());
        this.aircraftType = aircraftType;
        this.originIata = originIata;
        this.originIcao = originIcao;
        this.destinationIata = destinationIata;
        this.destinationIcao = destinationIcao;
        this.eta = eta;
    }

    public void calculateNextAircraftPosition() {
        this.aircraftPosition.setLatitude(this.aircraftPosition.getLatitude() + this.aircraftVector.getGroundSpeed() * Math.cos(Math.toRadians(this.aircraftVector.getHeading())));
        this.aircraftPosition.setLongitude(this.aircraftPosition.getLongitude() + this.aircraftVector.getGroundSpeed() * Math.sin(Math.toRadians(this.aircraftVector.getHeading())));
        this.aircraftPosition.setAltitude(this.aircraftPosition.getAltitude() + this.aircraftVector.getVerticalSpeed());
    }

    public void setInstructedVector(InstructedVector instructedVector) {
        this.instructedVector.setInstruction(instructedVector);
    }

    public void calculateNextAircraftVector() {
        this.aircraftVector.setHeading(this.instructedVector.getInstructedHeading());
        this.aircraftVector.setGroundSpeed(this.instructedVector.getInstructedGroundSpeed());
        this.aircraftVector.setVerticalSpeed((int) (this.instructedVector.getInstructedAltitude() - this.aircraftPosition.getAltitude()));
    }

    @Override
    public String toString() {
        return "CommercialAircraft{" +
                "callsign=" + this.callsign +
                ", position={" +
                "latitude=" + this.aircraftPosition.getLatitude() +
                ", longitude=" + this.aircraftPosition.getLongitude() +
                ", altitude=" + this.aircraftPosition.getAltitude() +
                "}, vector={" +
                "heading=" + this.aircraftVector.getHeading() +
                ", groundSpeed=" + this.aircraftVector.getGroundSpeed() +
                ", verticalSpeed=" + this.aircraftVector.getVerticalSpeed() +
                "}, type=" + this.aircraftType +
                ", originIata=" + this.originIata +
                ", originIcao=" + this.originIcao +
                ", destinationIata=" + this.destinationIata +
                ", destinationIcao=" + this.destinationIcao +
                ", eta=" + this.eta +
                '}';
    }

    public boolean isEqualCallsign(Callsign callsign) {
        return this.callsign.equals(callsign);
    }
}
