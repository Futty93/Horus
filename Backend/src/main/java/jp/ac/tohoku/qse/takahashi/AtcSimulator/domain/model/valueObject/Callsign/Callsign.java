package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign;

import java.util.Objects;

/**
 * 航空機のコールサインを表す値オブジェクト
 */
public class Callsign {
//    private final Company company;
//    private final FlightNumber flightNumber;
//
//    public Callsign(Company company, FlightNumber flightNumber) {
//        this.company = company;
//        this.flightNumber = flightNumber;
//    }
//
//    public Company getCompany() {
//        return this.company;
//    }
//
//    public FlightNumber getFlightNumber() {
//        return this.flightNumber;
//    }
//
//    public String toString() {
//        return this.company.name() + this.flightNumber.number();
//    }
//
//    public boolean equals(Callsign callsign){
//        return this.company.equals(callsign.getCompany()) && this.flightNumber.equals(callsign.getFlightNumber());
//    }
    private final String callsign;

    public Callsign(String callsign){
        this.callsign = callsign;
    }

    public String getCallsign(){
        return this.callsign;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Callsign that = (Callsign) obj;
        return Objects.equals(callsign, that.callsign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callsign);
    }

    @Override
    public String toString(){
        return this.callsign;
    }
}
