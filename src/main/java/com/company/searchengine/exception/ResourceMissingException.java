package com.company.searchengine.exception;

import static com.company.searchengine.exception.model.constant.ErrorCode.RESOURCE_MISSING;

import java.text.MessageFormat;

public class ResourceMissingException extends CommonException {

    public ResourceMissingException(String errorCode, String message) {
        super(errorCode, message);
    }

    public static ResourceMissingException of(String message, Object... args) {
        return new ResourceMissingException(RESOURCE_MISSING, MessageFormat.format(message, args));
    }

}
