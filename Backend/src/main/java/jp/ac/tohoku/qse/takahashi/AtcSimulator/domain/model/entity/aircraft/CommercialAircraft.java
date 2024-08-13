package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

public final class CommercialAircraft implements Aircraft {
    private double speed;
    private double heading;
    private double verticalSpeed;
    private AircraftPosition position;
    private final Callsign callsign;

    public CommercialAircraft(double speed, double altitude, double heading, double latitude, double longitude, double verticalSpeed, String companyName, String flightNumber) {
        this.position = new AircraftPosition(latitude, longitude, altitude);
        this.speed = speed;
        this.heading = heading;
        this.verticalSpeed = verticalSpeed;
        this.callsign = new Callsign(new Company(companyName), new FlightNumber(flightNumber));
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }


    public String getLocation() {
        return "Flight: " + this.callsign + ", Latitude: " + this.position.getLatitude() + ", Longitude: " + this.position.getLongitude();
    }

    public void NextStep() {
        this.position = new AircraftPosition(this.position.getLatitude(), this.position.getLongitude(), this.position.getAltitude() + this.verticalSpeed);
    }

    public boolean IsEqualCallsign(Callsign callsign){
        return this.callsign.equals(callsign);
    }
}
