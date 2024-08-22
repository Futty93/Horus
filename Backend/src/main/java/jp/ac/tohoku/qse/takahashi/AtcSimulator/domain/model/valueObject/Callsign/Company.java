package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign;

import java.util.Objects;

public class Company {

    private final String airlineCode;  // 航空会社コード (例: AAL)
    private final String airlineName;  // 航空会社名 (例: American Airlines)

    // コンストラクタ
    public Company(String airlineCode, String airlineName) {
        if (airlineCode == null || airlineCode.isEmpty()) {
            throw new IllegalArgumentException("Airline code cannot be null or empty");
        }
        if (airlineName == null || airlineName.isEmpty()) {
            throw new IllegalArgumentException("Airline name cannot be null or empty");
        }

        this.airlineCode = airlineCode;
        this.airlineName = airlineName;
    }

    // 航空会社コードを返すメソッド
    public String getAirlineCode() {
        return airlineCode;
    }

    // 航空会社名を返すメソッド
    public String getAirlineName() {
        return airlineName;
    }

    // equalsとhashCodeをオーバーライド
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return airlineCode.equals(company.airlineCode) && airlineName.equals(company.airlineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(airlineCode, airlineName);
    }

    // toStringメソッドをオーバーライド
    @Override
    public String toString() {
        return "Company{" +
                "airlineCode='" + airlineCode + '\'' +
                ", airlineName='" + airlineName + '\'' +
                '}';
    }
}