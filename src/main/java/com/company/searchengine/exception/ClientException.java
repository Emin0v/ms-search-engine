package com.company.searchengine.exception;

import lombok.Getter;

@Getter
public class ClientException extends CommonException {

    private final int status;
    private final String requestId;
    private final String errorCode;
    private final String message;

    public ClientException(int status, String requestId, String errorCode, String message) {
        super(errorCode, message);
        this.status = status;
        this.requestId = requestId;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ClientException of(int status, String requestId, String errorCode, String message) {
        return new ClientException(status, requestId, errorCode, message);
    }

}
