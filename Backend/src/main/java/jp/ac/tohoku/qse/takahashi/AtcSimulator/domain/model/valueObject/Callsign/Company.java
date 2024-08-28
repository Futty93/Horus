package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign;

public record Company(String name) {
    public Company(String name) {
        this.name = name.toUpperCase();
    }

}
