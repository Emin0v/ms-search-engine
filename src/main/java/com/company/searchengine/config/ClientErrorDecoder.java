package com.company.searchengine.config;

import com.company.searchengine.exception.ClientException;
import com.company.searchengine.exception.model.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ClientErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder defaultErrorDecoder;

    public ClientErrorDecoder(ObjectMapper objectMapper) {
        this.defaultErrorDecoder = new Default();
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try (InputStream bodyInputStream = response.body().asInputStream()) {
            ErrorResponse error = objectMapper.readValue(bodyInputStream, ErrorResponse.class);
            return ClientException.of(response.status(), error.getRequestId(),
                    error.getErrorCode(), error.getErrorMessage());
        } catch (Exception ex) {
            log.error("Exception occurs while parsing client error response: {}", response, ex);
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }

}
