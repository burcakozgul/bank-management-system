package com.burcak.mybank.exception;

public class AccountException extends RuntimeException{
    public AccountException(String message) {
        super(message);
    }
}
