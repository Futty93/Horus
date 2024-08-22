package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign;

import java.util.Objects;

public class FlightNumber {

    private final String flightNumber;  // フライト番号 (例: 1234)

    // コンストラクタ
    public FlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isEmpty()) {
            throw new IllegalArgumentException("Flight number cannot be null or empty");
        }

        if (!flightNumber.matches("\\d+")) {  // フライト番号が数字で構成されているか確認
            throw new IllegalArgumentException("Flight number must be a numeric value");
        }

        this.flightNumber = flightNumber;
    }

    // フライト番号を返すメソッド
    public String getFlightNumber() {
        return flightNumber;
    }

    // equalsとhashCodeをオーバーライド
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightNumber that = (FlightNumber) o;
        return flightNumber.equals(that.flightNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }

    // toStringメソッドをオーバーライド
    @Override
    public String toString() {
        return "FlightNumber{" +
                "flightNumber='" + flightNumber + '\'' +
                '}';
    }
}