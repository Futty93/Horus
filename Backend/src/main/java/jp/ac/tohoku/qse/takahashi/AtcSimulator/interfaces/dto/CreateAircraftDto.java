package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

public class CreateAircraftDto {
    public String companyName;
    public String flightNumber;
    public double latitude;
    public double longitude;
    public double altitude;

    public CreateAircraftDto(String companyName, String flightNumber, double latitude, double longitude, double altitude) {
        this.companyName = companyName;
        this.flightNumber = flightNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }
}
