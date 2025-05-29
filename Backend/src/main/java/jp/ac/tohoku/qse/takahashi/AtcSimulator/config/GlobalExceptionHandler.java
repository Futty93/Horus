package jp.ac.tohoku.qse.takahashi.AtcSimulator.config;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * グローバル例外ハンドラ
 *
 * アプリケーション全体で発生する例外を統一的に処理し、
 * 適切なHTTPステータスコードとエラーレスポンスを返します。
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String DEFAULT_ERROR_TYPE = "urn:problem-type:atc-simulator";

    /**
     * カスタム例外（ATCシミュレーター固有）の処理
     */
    @ExceptionHandler(AtcSimulatorException.class)
    public ResponseEntity<ErrorResponse> handleAtcSimulatorException(
            AtcSimulatorException ex, WebRequest request) {

        HttpStatus status = getHttpStatusFromExceptionType(ex.getExceptionType());

        logException(ex, status, request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":" + ex.getErrorCode().toLowerCase())
                .title(getStatusTitle(status))
                .status(status.value())
                .detail(ex.getUserMessage())
                .instance(request.getDescription(false))
                .errorCode(ex.getErrorCode())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * 航空機が見つからない例外の処理
     */
    @ExceptionHandler(AircraftNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAircraftNotFoundException(
            AircraftNotFoundException ex, WebRequest request) {

        logException(ex, HttpStatus.NOT_FOUND, request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":aircraft-not-found")
                .title("Aircraft Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getUserMessage())
                .instance(request.getDescription(false))
                .errorCode(ex.getErrorCode())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 航空機競合例外の処理
     */
    @ExceptionHandler(AircraftConflictException.class)
    public ResponseEntity<ErrorResponse> handleAircraftConflictException(
            AircraftConflictException ex, WebRequest request) {

        logException(ex, HttpStatus.CONFLICT, request);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":aircraft-conflict")
                .title("Aircraft Already Exists")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getUserMessage())
                .instance(request.getDescription(false))
                .errorCode(ex.getErrorCode())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * バリデーション例外の処理（Bean Validation）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("バリデーションエラー: {}", ex.getMessage());

        Map<String, Object> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":validation-error")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("入力データの検証に失敗しました")
                .instance(request.getDescription(false))
                .errorCode("VALIDATION_ERROR")
                .errors(validationErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 必須パラメータ不足の処理
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {

        logger.warn("必須パラメータ不足: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":missing-parameter")
                .title("Missing Required Parameter")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(String.format("必須パラメータ '%s' が不足しています", ex.getParameterName()))
                .instance(request.getDescription(false))
                .errorCode("MISSING_PARAMETER")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * パラメータタイプ不正の処理
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        logger.warn("パラメータタイプエラー: {}", ex.getMessage());

        String expectedType = ex.getRequiredType() != null ?
                ex.getRequiredType().getSimpleName() : "unknown";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":type-mismatch")
                .title("Parameter Type Mismatch")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(String.format("パラメータ '%s' の型が不正です。期待される型: %s",
                       ex.getName(), expectedType))
                .instance(request.getDescription(false))
                .errorCode("TYPE_MISMATCH")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * JSON解析エラーの処理
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseException(
            HttpMessageNotReadableException ex, WebRequest request) {

        logger.warn("JSON解析エラー: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":json-parse-error")
                .title("Invalid JSON Format")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("リクエストボディのJSON形式が無効です")
                .instance(request.getDescription(false))
                .errorCode("JSON_PARSE_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 一般的なRuntimeExceptionの処理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        logger.error("予期しないランタイムエラー: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":runtime-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("内部サーバーエラーが発生しました")
                .instance(request.getDescription(false))
                .errorCode("RUNTIME_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * その他の予期しない例外の処理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("予期しないエラー: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(DEFAULT_ERROR_TYPE + ":unexpected-error")
                .title("Unexpected Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("予期しないエラーが発生しました")
                .instance(request.getDescription(false))
                .errorCode("UNEXPECTED_ERROR")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 例外タイプからHTTPステータスを決定
     */
    private HttpStatus getHttpStatusFromExceptionType(AtcSimulatorException.ExceptionType type) {
        return switch (type) {
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case SYSTEM_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
            case EXTERNAL_API_ERROR -> HttpStatus.BAD_GATEWAY;
        };
    }

    /**
     * HTTPステータスからタイトルを取得
     */
    private String getStatusTitle(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Bad Request";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Conflict";
            case INTERNAL_SERVER_ERROR -> "Internal Server Error";
            case BAD_GATEWAY -> "Bad Gateway";
            default -> "Error";
        };
    }

    /**
     * 例外ログの統一出力
     */
    private void logException(Exception ex, HttpStatus status, WebRequest request) {
        String uri = request.getDescription(false);

        if (status.is4xxClientError()) {
            logger.warn("クライアントエラー [{}] {}: {} - URI: {}",
                       status.value(), status.getReasonPhrase(), ex.getMessage(), uri);
        } else {
            logger.error("サーバーエラー [{}] {}: {} - URI: {}",
                        status.value(), status.getReasonPhrase(), ex.getMessage(), uri, ex);
        }
    }
}
