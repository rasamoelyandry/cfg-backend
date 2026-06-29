package com.cfg.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final boolean success = false;
    private final int status;
    private final String error;
    private final String message;
    private final Map<String, String> fieldErrors;
    private final Instant timestamp;

    public static ErrorResponse of(int status, String error, String message) {
        return ErrorResponse.builder()
                .status(status).error(error).message(message)
                .timestamp(Instant.now()).build();
    }
}
