package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RFC 7807 Problem Details for HTTP APIs形式に基づくエラーレスポンス
 *
 * 標準化されたエラー情報の構造を提供し、クライアントが一貫した方法で
 * エラー情報を処理できるようにします。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * 問題の種別を特定するURI参照
     */
    @JsonProperty("type")
    private final String type;

    /**
     * 問題の短い、人間が読める要約
     */
    @JsonProperty("title")
    private final String title;

    /**
     * HTTPステータスコード
     */
    @JsonProperty("status")
    private final int status;

    /**
     * 具体的なエラーの詳細情報
     */
    @JsonProperty("detail")
    private final String detail;

    /**
     * 問題が発生した特定のURIリソース
     */
    @JsonProperty("instance")
    private final String instance;

    /**
     * エラーコード（システム固有）
     */
    @JsonProperty("errorCode")
    private final String errorCode;

    /**
     * エラー発生時刻
     */
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    /**
     * 追加のエラー詳細情報（フィールドエラー等）
     */
    @JsonProperty("errors")
    private final Map<String, Object> errors;

    /**
     * コンストラクタ
     *
     * @param builder ErrorResponseBuilderインスタンス
     */
    private ErrorResponse(ErrorResponseBuilder builder) {
        this.type = builder.type;
        this.title = builder.title;
        this.status = builder.status;
        this.detail = builder.detail;
        this.instance = builder.instance;
        this.errorCode = builder.errorCode;
        this.timestamp = builder.timestamp != null ? builder.timestamp : LocalDateTime.now();
        this.errors = builder.errors;
    }

    // Getters
    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getDetail() {
        return detail;
    }

    public String getInstance() {
        return instance;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getErrors() {
        return errors;
    }

    /**
     * ErrorResponseBuilderを取得
     *
     * @return 新しいErrorResponseBuilderインスタンス
     */
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    /**
     * ErrorResponseのBuilderクラス
     */
    public static class ErrorResponseBuilder {
        private String type;
        private String title;
        private int status;
        private String detail;
        private String instance;
        private String errorCode;
        private LocalDateTime timestamp;
        private Map<String, Object> errors;

        public ErrorResponseBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ErrorResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public ErrorResponseBuilder instance(String instance) {
            this.instance = instance;
            return this;
        }

        public ErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ErrorResponseBuilder errors(Map<String, Object> errors) {
            this.errors = errors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
