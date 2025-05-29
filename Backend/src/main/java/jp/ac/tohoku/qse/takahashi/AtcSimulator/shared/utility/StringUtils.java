package jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility;

import java.util.Objects;

/**
 * 文字列処理の共通ユーティリティクラス
 * フォーマッティング、パース処理など重複しがちな文字列操作を集約
 */
public final class StringUtils {

    // ユーティリティクラスのためプライベートコンストラクタ
    private StringUtils() {
        throw new AssertionError("ユーティリティクラスのインスタンス化は禁止されています");
    }

    /**
     * 文字列がnullまたは空文字列かを判定
     *
     * @param str 文字列
     * @return nullまたは空文字列の場合true
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 文字列がnullまたは空白文字のみかを判定
     *
     * @param str 文字列
     * @return nullまたは空白文字のみの場合true
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 航空機のコールサインを文字列から抽出
     *
     * @param line 航空機情報を含む文字列
     * @return コールサイン
     */
    public static String extractCallsignFromLine(String line) {
        if (isNullOrBlank(line)) {
            return "";
        }

        int start = line.indexOf("callsign=");
        if (start == -1) {
            return "";
        }

        start += 9; // "callsign="の長さ
        int end = line.indexOf(",", start);
        if (end == -1) {
            end = line.indexOf("}", start);
        }

        return end > start ? line.substring(start, end) : "";
    }

    /**
     * 航空機のレーダー表示用基本情報フォーマット
     * 各航空機タイプで共通の基本情報部分
     *
     * @param callsign コールサイン
     * @param lat 緯度
     * @param lon 経度
     * @param alt 高度
     * @param hdg ヘディング
     * @param gs 地上速度
     * @param vs 垂直速度
     * @param iHdg 指示ヘディング
     * @param iGs 指示地上速度
     * @param iAlt 指示高度
     * @param type 航空機カテゴリ
     * @param model 航空機機種
     * @return フォーマットされた基本情報文字列
     */
    public static String formatAircraftBaseInfo(String callsign, double lat, double lon, double alt,
                                              double hdg, double gs, double vs,
                                              double iHdg, double iGs, double iAlt,
                                              String type, String model) {
        return String.format("Aircraft{callsign=%s, position={latitude=%.6f, longitude=%.6f, altitude=%.0f}, " +
                "vector={heading=%.1f, groundSpeed=%.1f, verticalSpeed=%.0f}, " +
                "instructedVector={heading=%.1f, groundSpeed=%.1f, altitude=%.0f}, " +
                "type=%s, model=%s",
                callsign, lat, lon, alt, hdg, gs, vs, iHdg, iGs, iAlt, type, model);
    }

    /**
     * 航空機の簡潔な表示用フォーマット
     * デバッグ用やログ用の短縮版
     *
     * @param callsign コールサイン
     * @param lat 緯度
     * @param lon 経度
     * @param alt 高度
     * @param hdg ヘディング
     * @param gs 地上速度
     * @param vs 垂直速度
     * @return フォーマットされた簡潔情報文字列
     */
    public static String formatAircraftShortInfo(String callsign, double lat, double lon, double alt,
                                                double hdg, double gs, double vs) {
        return String.format("callsign=%s, position={lat=%.4f, lon=%.4f, alt=%.0f}, " +
                "vector={hdg=%.1f, spd=%.1f, vs=%.0f}",
                callsign, lat, lon, alt, hdg, gs, vs);
    }

    /**
     * 2つの文字列を航空機ペアIDとして結合
     * アルファベット順で並び替えて一意性を保証
     *
     * @param callsign1 航空機1のコールサイン
     * @param callsign2 航空機2のコールサイン
     * @return 航空機ペアID
     */
    public static String generatePairId(String callsign1, String callsign2) {
        Objects.requireNonNull(callsign1, "callsign1がnullです");
        Objects.requireNonNull(callsign2, "callsign2がnullです");

        if (callsign1.compareTo(callsign2) < 0) {
            return callsign1 + "-" + callsign2;
        } else {
            return callsign2 + "-" + callsign1;
        }
    }

    /**
     * 危険度表示のフォーマット
     *
     * @param riskLevel 危険度（0-100）
     * @return フォーマットされた危険度文字列
     */
    public static String formatRiskLevel(double riskLevel) {
        return String.format("%.2f", riskLevel);
    }

    /**
     * 文字列中の特定パターンを正規表現で抽出
     *
     * @param input 入力文字列
     * @param pattern 正規表現パターン
     * @param groupIndex 抽出するグループのインデックス
     * @return 抽出された文字列、見つからない場合は空文字列
     */
    public static String extractByRegex(String input, String pattern, int groupIndex) {
        if (isNullOrBlank(input) || isNullOrBlank(pattern)) {
            return "";
        }

        try {
            var matcher = java.util.regex.Pattern.compile(pattern).matcher(input);
            return matcher.find() && matcher.groupCount() >= groupIndex ?
                   matcher.group(groupIndex) : "";
        } catch (Exception e) {
            return "";
        }
    }
}
