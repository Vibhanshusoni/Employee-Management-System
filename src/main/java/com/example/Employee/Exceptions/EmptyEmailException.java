package com.example.Employee.Exceptions;

public class EmptyEmailException extends RuntimeException {
    public EmptyEmailException(String message) {
        super(message);
    }
}
