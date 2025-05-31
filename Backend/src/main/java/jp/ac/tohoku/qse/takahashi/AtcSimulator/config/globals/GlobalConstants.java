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

    // ========== 最適化関連の定数 ==========

    /** 計算効率化のためのキャッシュ済み定数 */
    public static final double RADIANS_TO_DEGREES = 180.0 / Math.PI;
    public static final double PI_OVER_180 = Math.PI / 180.0;
    public static final double HALF_PI = Math.PI / 2.0;
    public static final double TWO_PI = 2.0 * Math.PI;

    /** 三角関数計算の最適化用定数 */
    public static final double SIN_COS_CACHE_PRECISION = 1000.0; // 0.001度の精度
    public static final int SIN_COS_CACHE_SIZE = (int)(360.0 * SIN_COS_CACHE_PRECISION);

    /** 地球関連の事前計算定数 */
    public static final double EARTH_RADIUS_NM = EARTH_RADIUS / NAUTICAL_MILES_TO_KM; // 海里での地球半径
    public static final double EARTH_CIRCUMFERENCE_KM = 2.0 * Math.PI * EARTH_RADIUS;
    public static final double EARTH_CIRCUMFERENCE_NM = EARTH_CIRCUMFERENCE_KM / NAUTICAL_MILES_TO_KM;

    /** 単位変換の最適化用定数 */
    public static final double KM_PER_HOUR_TO_KNOTS = 1.0 / KNOTS_TO_KM_PER_HOUR;
    public static final double KNOTS_TO_METERS_PER_SECOND = KNOTS_TO_KM_PER_HOUR * 1000.0 / 3600.0;
    public static final double FEET_PER_MINUTE_TO_METERS_PER_SECOND = FEET_TO_METERS / 60.0;

    /** CommercialAircraft最適化用定数 */
    public static final double POSITION_UPDATE_THRESHOLD = 0.1; // 位置更新の最小しきい値（海里）
    public static final double HEADING_UPDATE_THRESHOLD = 0.1; // ヘディング更新の最小しきい値（度）
    public static final double SPEED_UPDATE_THRESHOLD = 0.1; // 速度更新の最小しきい値（ノット）
    public static final double ALTITUDE_UPDATE_THRESHOLD = 5.0; // 高度更新の最小しきい値（フィート）- より厳格に設定

    /** パフォーマンス最適化用定数 */
    public static final double FLOATING_POINT_EPSILON = 1e-9; // 浮動小数点比較の許容誤差
    public static final double ANGULAR_EPSILON = 1e-6; // 角度計算の許容誤差（度）
    public static final double DISTANCE_EPSILON = 1e-3; // 距離計算の許容誤差（海里）

    /** メモリ最適化用定数 */
    public static final int POSITION_CACHE_SIZE = 1000; // 位置計算キャッシュサイズ
    public static final int VECTOR_POOL_SIZE = 500; // ベクトルオブジェクトプールサイズ
}
