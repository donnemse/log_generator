package com.igloosec.generator.restful.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author TheHoang
 */
@Data
@AllArgsConstructor
public abstract class AObjectResponse {
    private int status;
    private String msg;

    public AObjectResponse() {
        status = HttpStatus.OK.value();
    }

    public AObjectResponse(int stat) {
        status = stat;
    }

    public AObjectResponse(String message) {
        msg = message;
        status = HttpStatus.OK.value();
    }
}
