package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

/**
 * 航空機のナビゲーションモード。
 * 管制官の指示とフライトプランの切り替えを明確に区別する。
 */
public enum NavigationMode {
    /** フライトプランに従って自動飛行。ウェイポイントを順次通過し、高度・速度制約を適用 */
    FLIGHT_PLAN,

    /** 管制官のヘディング指示に従う。フライトプランから離脱した状態 */
    HEADING,

    /** 指定された Fix へ直行。到達後は resume 設定に応じて FLIGHT_PLAN または HEADING へ */
    DIRECT_TO
}
