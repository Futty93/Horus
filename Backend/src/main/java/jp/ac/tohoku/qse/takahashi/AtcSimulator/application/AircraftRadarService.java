package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public interface AircraftRadarService {
    /**
     * コールサインを受け取り、該当する航空機の位置を取得する
     *
     * @param callsign
     * @return
     */
    String getAircraftLocation(Callsign callsign);

    /**
     * 全ての航空機の位置を取得する
     *
     * @return
     */
    String getAllAircraftLocation();
}
