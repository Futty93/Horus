package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type;

/**
 * ILS（Instrument Landing System）のタイプを表す値オブジェクト
 */
public class IlsType {
    private final String type;

    public IlsType(String type) {
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
        IlsType ilsType = (IlsType) obj;
        return type != null ? type.equals(ilsType.type) : ilsType.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
