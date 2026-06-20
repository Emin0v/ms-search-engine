package com.company.searchengine.exception.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    private String errorCode;
    private String errorMessage;
    private String requestId;
    private LocalDateTime timestamp;

    public ErrorResponse(String errorCode, String errorMessage, String requestId) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.requestId = requestId;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String errorCode, String errorMessage, String requestId) {
        return new ErrorResponse(errorCode, errorMessage, requestId);
    }

}

