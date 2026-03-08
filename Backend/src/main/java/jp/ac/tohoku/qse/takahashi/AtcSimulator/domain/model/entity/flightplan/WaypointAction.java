package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

/**
 * ウェイポイント通過時のアクション。
 */
public enum WaypointAction {
    /** 次のウェイポイントへ継続 */
    CONTINUE,

    /** 航空機を削除（タワー移管を想定） */
    REMOVE_AIRCRAFT,

    /** 管制移管（将来拡張用） */
    HANDOFF
}
