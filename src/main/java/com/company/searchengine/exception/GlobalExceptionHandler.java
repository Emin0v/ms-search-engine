package com.company.searchengine.exception;

import static com.company.searchengine.exception.model.constant.ErrorCode.DATA_NOT_FOUND;
import static com.company.searchengine.exception.model.constant.ErrorCode.INTERNAL_SERVER;
import static com.company.searchengine.exception.model.constant.ErrorCode.REQUEST_INVALID;
import static com.company.searchengine.exception.model.constant.ErrorMessage.CONSTRAINT_VIOLATION_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.INTERNAL_SERVER_ERROR_MESSAGE;
import static com.company.searchengine.exception.model.constant.ErrorMessage.INVALID_VALUE_ERROR_MESSAGE;
import static com.company.searchengine.model.constant.ApplicationConstants.HttpAttribute.ELAPSED_TIME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static net.logstash.logback.argument.StructuredArguments.v;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.company.searchengine.exception.model.ErrorResponse;
import com.company.searchengine.exception.model.constant.ErrorCode;
import com.company.searchengine.util.MessageSourceUtil;
import com.company.searchengine.util.WebUtil;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Component
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private WebUtil webUtil;

    @Autowired
    private MessageSourceUtil messageSourceUtil;

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(DataNotFoundException ex) {
        addErrorLog(NOT_FOUND, ex.getMessage(), "DataNotFoundException");
        return ResponseEntity.status(NOT_FOUND)
                .body(ErrorResponse.of(DATA_NOT_FOUND, ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        addErrorLog(BAD_REQUEST, ex.getErrorCode(), ex.getErrorMessage(), "InvalidInputException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(ResourceMissingException.class)
    public ResponseEntity<ErrorResponse> handleResourceMissingException(ResourceMissingException ex) {
        addErrorLog(NOT_FOUND, ex.getErrorCode(), ex.getErrorMessage(), "ResourceMissingException");
        return ResponseEntity.status(NOT_FOUND)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        addErrorLog(BAD_REQUEST, ex.getErrorCode(), ex.getErrorMessage(), "BadRequestException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistException(AlreadyExistException ex) {
        addErrorLog(BAD_REQUEST, ex.getErrorCode(), ex.getErrorMessage(), "AlreadyExistException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getErrorMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        addErrorLog(BAD_REQUEST, ex.getMessage(), "MethodArgumentTypeMismatchException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleMethodIllegalArgumentException(IllegalArgumentException ex) {
        addErrorLog(BAD_REQUEST, ex.getMessage(), "IllegalArgumentException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        addErrorLog(BAD_REQUEST, ex.getMessage(), "IllegalStateException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, ex.getMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        String errMsg = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + messageSourceUtil.getMessage(v.getMessage()))
                .collect(collectingAndThen(joining("; "), Optional::ofNullable))
                .orElse(ex.getMessage());

        addErrorLog(BAD_REQUEST, errMsg, "ConstraintViolationException");
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.BAD_REQUEST, errMsg, webUtil.getRequestId()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String fieldName = extractFieldNameFromMessage(ex.getMostSpecificCause().getMessage());
        String errorMessage = (fieldName != null)
                ? MessageFormat.format(INVALID_VALUE_ERROR_MESSAGE, fieldName) : CONSTRAINT_VIOLATION_ERROR_MESSAGE;

        addErrorLog(CONFLICT, errorMessage, "DataIntegrityViolationException");
        return ResponseEntity.status(CONFLICT)
                .body(ErrorResponse.of(ErrorCode.CONFLICT, errorMessage, webUtil.getRequestId()));
    }

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ClientException ex) {
        final HttpStatus httpStatus = HttpStatus.valueOf(ex.getStatus());
        addErrorLog(httpStatus, ex.getErrorCode(), ex.getMessage(), "ClientException");
        final ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), ex.getRequestId());
        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponse> handleCommonException(CommonException ex) {
        addErrorLog(INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage(), webUtil.getRequestId()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        addErrorLog(INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getMessage(), ex);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(INTERNAL_SERVER, INTERNAL_SERVER_ERROR_MESSAGE, webUtil.getRequestId()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NotNull HttpHeaders headers,
                                                                  @NotNull HttpStatusCode statusCode,
                                                                  @NotNull WebRequest request) {
        Optional<FieldError> fieldError = ex.getBindingResult().getFieldErrors().stream().findAny();
        String errorMessage;
        String errorCode;
        if (fieldError.isPresent()) {
            String fieldName = fieldError.get().getField();
            String fieldMessage = fieldError.get().getDefaultMessage();
            errorMessage = fieldName + " " + messageSourceUtil.getMessage(fieldMessage);
            errorCode = messageSourceUtil.getErrorCode(fieldMessage, REQUEST_INVALID);
        } else {
            errorMessage = ex.getMessage();
            errorCode = ErrorCode.REQUEST_INVALID;
        }
        addErrorLog(httpStatus(statusCode), errorCode, errorMessage, "MethodArgumentNotValidException");
        ErrorResponse errorResponse = new ErrorResponse(ErrorCode.BAD_REQUEST, errorMessage, webUtil.getRequestId());
        return new ResponseEntity<>(errorResponse, headers, statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     @NotNull HttpHeaders headers,
                                                                     HttpStatusCode status,
                                                                     @NotNull WebRequest request) {
        String error = ex.getRequestPartName() + " parameter is missing";
        addErrorLog((HttpStatus) status, error, "MissingServletRequestParameterException");
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, error, webUtil.getRequestId());
        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  @NotNull HttpHeaders headers,
                                                                  HttpStatusCode status, @NotNull WebRequest request) {
        String errLogMessage = String.join(System.lineSeparator(),
                ex.getMessage(), "Request body: " + webUtil.getRequestBody());
        String error = "Request not readable";
        addErrorLog((HttpStatus) status, errLogMessage, "HttpMessageNotReadableException");
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.BAD_REQUEST, error, webUtil.getRequestId());
        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    private String extractFieldNameFromMessage(String message) {
        Pattern pattern = Pattern.compile("Key \\((.*?)\\)");
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : null;
    }

    protected HttpStatus httpStatus(int httpStatus, HttpStatus defaultHttpStatus) {
        return Optional.ofNullable(HttpStatus.resolve(httpStatus)).orElse(defaultHttpStatus);
    }

    protected HttpStatus httpStatus(HttpStatusCode httpStatusCode) {
        return HttpStatus.resolve(httpStatusCode.value());
    }

    /*** Logging ***/

    protected void addErrorLog(HttpStatus httpStatus, String errorMessage, String exceptionType) {
        addErrorLog(httpStatus.value(), errorMessage, exceptionType);
    }

    protected void addErrorLog(Integer errorCode, String errorMessage, Throwable ex) {
        log.error("[Error] | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                errorCode, ex.getClass().getTypeName(), webUtil.getRequestUri(),
                v(ELAPSED_TIME, webUtil.getElapsedTime()), errorMessage, ex);
    }

    protected void addErrorLog(Integer errorCode, String errorMessage, String exceptionType) {
        log.error("[Error] | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                errorCode, exceptionType, webUtil.getRequestUri(),
                v(ELAPSED_TIME, webUtil.getElapsedTime()), errorMessage);
    }

    protected void addErrorLog(HttpStatus httpStatus, String errorCode, String errorMessage, Throwable ex) {
        log.error("[Error] | Status: {} | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                httpStatus, errorCode, ex.getClass().getTypeName(), webUtil.getRequestUri(),
                v(ELAPSED_TIME, webUtil.getElapsedTime()), errorMessage, ex);
    }

    protected void addErrorLog(HttpStatus httpStatus, String errorCode, String errorMessage, String exceptionType) {
        log.error("[Error] | Status: {} | Code: {} | Type: {} | Path: {} | Elapsed time: {} ms | Message: {}",
                httpStatus, errorCode, exceptionType, webUtil.getRequestUri(),
                v(ELAPSED_TIME, webUtil.getElapsedTime()), errorMessage);
    }

}
