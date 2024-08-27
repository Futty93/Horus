package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public class CommercialAircraft extends Aircraft {
    private String originIate;
    private String originIcao;
    private String destinationIate;
    private String destinationIcao;
    private String eta; //Estimated Time of Arrival in ISO 8601 format

    //Constructor
    public CommercialAircraft(Callsign callsign, AircraftPosition aircraftPosition, AircraftVector aircraftVector, AircraftType aircraftType, String originIate, String originIcao, String destinationIate, String destinationIcao, String eta) {
        super(callsign, aircraftPosition, aircraftVector, aircraftType);
        this.originIate = originIate;
        this.originIcao = originIcao;
        this.destinationIate = destinationIate;
        this.destinationIcao = destinationIcao;
        this.eta = eta;
    }

    public String getLocation() {
        return "Flight: " + this.getCallsign() + ", Latitude: " + this.getAircraftPosition().getLatitude() + ", Longitude: " + this.getAircraftPosition().getLongitude();
    }
}
