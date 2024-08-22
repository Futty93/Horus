package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign;

import java.util.Objects;

public class Callsign {

    private final String airlineCode;
    private final String flightNumber;

    // コンストラクタ
    public Callsign(String airlineCode, String flightNumber) {
        if (airlineCode == null || airlineCode.isEmpty()) {
            throw new IllegalArgumentException("Airline code cannot be null or empty");
        }
        if (flightNumber == null || flightNumber.isEmpty()) {
            throw new IllegalArgumentException("Flight number cannot be null or empty");
        }

        this.airlineCode = airlineCode;
        this.flightNumber = flightNumber;
    }

    // コールサインを完全な形式で返すメソッド
    public String getFullCallsign() {
        return airlineCode + flightNumber;
    }

    // 航空会社コードを返すメソッド
    public String getAirlineCode() {
        return airlineCode;
    }

    // フライトナンバーを返すメソッド
    public String getFlightNumber() {
        return flightNumber;
    }

    // equalsとhashCodeをオーバーライド
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Callsign callsign = (Callsign) o;
        return airlineCode.equals(callsign.airlineCode) && flightNumber.equals(callsign.flightNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airlineCode, flightNumber);
    }

    // toStringメソッドをオーバーライド
    @Override
    public String toString() {
        return "Callsign{" +
                "airlineCode='" + airlineCode + '\'' +
                ", flightNumber='" + flightNumber + '\'' +
                '}';
    }
}