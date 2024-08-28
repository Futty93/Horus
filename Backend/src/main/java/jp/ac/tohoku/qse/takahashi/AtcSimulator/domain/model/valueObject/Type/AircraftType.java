package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type;

public class AircraftType {
    private final String type;

    public AircraftType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return this.type;
    }

    public boolean equals(AircraftType aircraftType) {
        return this.type.equals(aircraftType.getType());
    }

}
