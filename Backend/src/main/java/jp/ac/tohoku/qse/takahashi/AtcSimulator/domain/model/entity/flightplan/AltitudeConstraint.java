package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

/**
 * ウェイポイントにおける高度制約の種類。
 */
public enum AltitudeConstraint {
    /** 指定高度ちょうど */
    AT,

    /** 指定高度以上 */
    AT_OR_ABOVE,

    /** 指定高度以下 */
    AT_OR_BELOW,

    /** 指定範囲内（将来拡張用） */
    BETWEEN,

    /** 制約なし */
    NONE
}
