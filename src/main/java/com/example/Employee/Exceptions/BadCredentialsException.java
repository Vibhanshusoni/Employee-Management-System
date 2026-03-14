package com.example.Employee.Exceptions;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {

        super(message);
    }
}
