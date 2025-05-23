package jp.ac.tohoku.qse.takahashi.AtcSimulator.config.globals;

public class GlobalConstants {
    public static final int REFRESH_RATE = 1;
    public static final double EARTH_RADIUS = 6378.1; //赤道半径 [km]
    public static final double KNOTS_TO_KM_PER_HOUR = 1.852; //ノットからkm/hに変換するための係数　1ノット = 1.852 km/h

    // コンフリクト検出関連の定数

    /** 管制間隔 - 水平方向（海里） */
    public static final double MINIMUM_HORIZONTAL_SEPARATION = 5.0;

    /** 管制間隔 - 垂直方向（フィート） */
    public static final double MINIMUM_VERTICAL_SEPARATION = 1000.0;

    /** 予測時間の最大値（秒） - 5分 */
    public static final double MAX_PREDICTION_TIME = 300.0;

    /** 事前フィルタリングのための最大検討距離（海里） */
    public static final double MAX_CONSIDERATION_DISTANCE = 50.0;

    /** ノーチカルマイルからキロメートルの変換係数 */
    public static final double NAUTICAL_MILES_TO_KM = 1.852;

    /** フィートからメートルの変換係数 */
    public static final double FEET_TO_METERS = 0.3048;

    /** 度をラジアンに変換する係数 */
    public static final double DEGREES_TO_RADIANS = Math.PI / 180.0;
}
