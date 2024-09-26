package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftHighlight;

public class CallsignExtructStatus {
    public static final CallsignExtructStatus SUCCESS = new CallsignExtructStatus("SUCCESS");
    public static final CallsignExtructStatus FAILURE = new CallsignExtructStatus("FAILURE");
    public static final CallsignExtructStatus NO_VALUE = new CallsignExtructStatus("NO_VALUE");

    private final String status;

    // プライベートコンストラクタで外部からのインスタンス生成を制限
    private CallsignExtructStatus(String status) {
        if (!status.equals("SUCCESS") && !status.equals("FAILURE") && !status.equals("NO_VALUE")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        this.status = status;
    }

    // ステータスが成功かどうかを確認
    public boolean isSuccess() {
        return this.status.equals("SUCCESS");
    }

    // ステータスが失敗かどうかを確認
    public boolean isFailure() {
        return this.status.equals("FAILURE");
    }

    // ステータスが値なし（NO_VALUE）かどうかを確認
    public boolean isNoValue() {
        return this.status.equals("NO_VALUE");
    }

    // ステータスを文字列として取得
    @Override
    public String toString() {
        return this.status;
    }
}
