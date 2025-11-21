package com.etd.reservation_management.exception;

import lombok.Getter;

@Getter
public class IllegalStateException extends RuntimeException {

    private final String fieldName;

    public IllegalStateException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

}
