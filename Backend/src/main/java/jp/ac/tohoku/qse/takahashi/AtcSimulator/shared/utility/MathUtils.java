package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

/**
 * 数学的計算の共通ユーティリティクラス
 * 角度計算、変換処理など重複しがちな計算を集約
 */
public final class MathUtils {

    // ユーティリティクラスのためプライベートコンストラクタ
    private MathUtils() {
        throw new AssertionError("ユーティリティクラスのインスタンス化は禁止されています");
    }

    /**
     * 角度を0-360度の範囲に正規化
     *
     * @param angle 角度（度）
     * @return 正規化された角度（0-360度）
     */
    public static double normalizeAngle(double angle) {
        return ((angle % 360) + 360) % 360;
    }

    /**
     * 角度を-180度から180度の範囲に正規化
     *
     * @param angle 角度（度）
     * @return 正規化された角度（-180-180度）
     */
    public static double normalizeAngleTo180(double angle) {
        return ((angle + 180) % 360 + 360) % 360 - 180;
    }

    /**
     * 度をラジアンに変換
     *
     * @param degrees 度
     * @return ラジアン
     */
    public static double toRadians(double degrees) {
        return Math.toRadians(degrees);
    }

    /**
     * ラジアンを度に変換
     *
     * @param radians ラジアン
     * @return 度
     */
    public static double toDegrees(double radians) {
        return Math.toDegrees(radians);
    }

    /**
     * 緯度を有効範囲（-90度から90度）に正規化
     *
     * @param latitude 緯度
     * @return 正規化された緯度
     */
    public static double normalizeLatitude(double latitude) {
        return Math.max(-90, Math.min(90, latitude));
    }

    /**
     * 経度を有効範囲（-180度から180度）に正規化
     *
     * @param longitude 経度
     * @return 正規化された経度
     */
    public static double normalizeLongitude(double longitude) {
        return normalizeAngleTo180(longitude);
    }

    /**
     * 2つの値の間の線形補間
     *
     * @param start 開始値
     * @param end 終了値
     * @param factor 補間係数（0.0-1.0）
     * @return 補間された値
     */
    public static double lerp(double start, double end, double factor) {
        return start + factor * (end - start);
    }

    /**
     * 値を指定された範囲に制限
     *
     * @param value 値
     * @param min 最小値
     * @param max 最大値
     * @return 制限された値
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 2つの値が指定された許容誤差内で等しいかを判定
     *
     * @param a 値1
     * @param b 値2
     * @param epsilon 許容誤差
     * @return 等しい場合true
     */
    public static boolean approximately(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    /**
     * 2つの値が浮動小数点の精度で等しいかを判定
     *
     * @param a 値1
     * @param b 値2
     * @return 等しい場合true
     */
    public static boolean approximately(double a, double b) {
        return approximately(a, b, 1e-9);
    }
}
