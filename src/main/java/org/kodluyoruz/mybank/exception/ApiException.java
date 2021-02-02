package org.kodluyoruz.mybank.exception;

public class ApiException {
    private final String message;

    public ApiException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
