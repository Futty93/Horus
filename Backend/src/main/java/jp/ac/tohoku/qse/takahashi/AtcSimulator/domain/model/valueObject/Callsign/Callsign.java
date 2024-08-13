package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign;

public class Callsign {
    private final Company company;
    private final FlightNumber flightNumber;

    public Callsign(Company company, FlightNumber flightNumber) {
        this.company = company;
        this.flightNumber = flightNumber;
    }

    public Company getCompany() {
        return this.company;
    }

    public FlightNumber getFlightNumber() {
        return this.flightNumber;
    }

    public String toString() {
        return this.company.name() + this.flightNumber.number();
    }

    public boolean equals(Callsign callsign){
        return this.company.equals(callsign.getCompany()) && this.flightNumber.equals(callsign.getFlightNumber());
    }
}
