package com.campuslfp.exception;

import org.springframework.http.HttpStatus;

public class DatabaseIntegrityException extends ApiException {
    public DatabaseIntegrityException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
