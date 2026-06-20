package com.company.searchengine.exception;

import static com.company.searchengine.exception.model.constant.ErrorCode.BAD_REQUEST;

import java.text.MessageFormat;

public class InvalidInputException extends CommonException {

    public InvalidInputException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static InvalidInputException of(String message, Object... args) {
        return new InvalidInputException(BAD_REQUEST, MessageFormat.format(message, args));
    }

}
