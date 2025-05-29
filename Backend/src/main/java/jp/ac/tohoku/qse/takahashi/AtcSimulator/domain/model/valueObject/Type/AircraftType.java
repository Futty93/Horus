package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type;

/**
 * 航空機タイプを表す値オブジェクト
 */
public class AircraftType {
    private final String type;

    public AircraftType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AircraftType that = (AircraftType) obj;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
