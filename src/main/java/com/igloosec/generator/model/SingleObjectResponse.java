package com.igloosec.generator.model;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author TheHoang
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class SingleObjectResponse extends AObjectResponse {
    private Object data;

    public SingleObjectResponse(int stat, String message, Object d) {
        super(stat, message);
        this.data = d;
    }

    public SingleObjectResponse(int stat, String message) {
        super(stat, message);
    }

    public SingleObjectResponse(String message, Object d) {
        super(HttpStatus.OK.value(), message);
        this.data = d;
    }

    public SingleObjectResponse(String message) {
        super(HttpStatus.OK.value(), message);
    }
}
