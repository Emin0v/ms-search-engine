package com.company.searchengine.exception;

import java.text.MessageFormat;

public class BadRequestException extends CommonException {

    public BadRequestException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static BadRequestException of(String errorCode, String message, Object... args) {
        return new BadRequestException(errorCode, MessageFormat.format(message, args));
    }

}
