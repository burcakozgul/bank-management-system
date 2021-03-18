package com.burcak.mybank.exception;

public class CreditCardException extends RuntimeException{
    public CreditCardException(String message) {
        super(message);
    }
}
