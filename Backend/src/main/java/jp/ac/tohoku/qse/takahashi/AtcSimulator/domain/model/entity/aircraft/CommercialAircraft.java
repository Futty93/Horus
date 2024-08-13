package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;

public final class CommercialAircraft implements Aircraft {
    private double speed_kt;
    private double heading;
    private double verticalSpeed_ft_sec;
    private AircraftPosition position;
    private final Callsign callsign;

    public CommercialAircraft(double speed_kt, double altitude, double heading, double latitude, double longitude, double verticalSpeed_ft_sec, String companyName, String flightNumber) {
        this.position = new AircraftPosition(latitude, longitude, altitude);
        this.speed_kt = speed_kt;
        this.heading = heading;
        this.verticalSpeed_ft_sec = verticalSpeed_ft_sec;
        this.callsign = new Callsign(new Company(companyName), new FlightNumber(flightNumber));
    }

    public void setSpeed_kt(double speed_kt) {
        this.speed_kt = speed_kt;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setVerticalSpeed_ft_sec(double verticalSpeed_ft_sec) {
        this.verticalSpeed_ft_sec = verticalSpeed_ft_sec;
    }


    public String getLocation() {
        return "Flight: " + this.callsign + ", Latitude: " + this.position.getLatitude() + ", Longitude: " + this.position.getLongitude();
    }

    public void NextStep() {
        this.position = new AircraftPosition(this.position.getLatitude()+speed_kt*Math.sin(heading*Math.PI/180), this.position.getLongitude()+speed_kt*Math.cos(heading*Math.PI/180), this.position.getAltitude() + this.verticalSpeed_ft_sec);
    }

    public boolean IsEqualCallsign(Callsign callsign){
        return this.callsign.equals(callsign);
    }
}
