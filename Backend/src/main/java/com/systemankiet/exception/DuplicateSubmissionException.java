package com.systemankiet.exception;

/**
 * Rzucany gdy użytkownik próbuje ponownie wypełnić ankietę, którą już wypełnił.
 * Mapowany na 409 Conflict przez GlobalExceptionHandler.
 */
public class DuplicateSubmissionException extends RuntimeException {
    public DuplicateSubmissionException(String message) {
        super(message);
    }
}
