package com.example.Employee.Exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /*-------------------------------- EMPLOYEE NOT FOUND --------------------------------*/

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                "Employee not found",
                LocalDateTime.now().plusMinutes(15)
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /*-------------------------------- USERNAME ALREADY EXISTS --------------------------------*/

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                "Username already exists",
                LocalDateTime.now().plusMinutes(15)
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /*-------------------------------- BAD CREDENTIALS --------------------------------*/

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid username or password",
                request.getRequestURI(),
                "Authentication failed",
                LocalDateTime.now().plusMinutes(15)
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /*-------------------------------- JWT TOKEN EXPIRED --------------------------------*/

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredToken(
            ExpiredJwtException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token expired",
                request.getRequestURI(),
                "Please login again",
                LocalDateTime.now().plusMinutes(15)
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /*-------------------------------- SPRING VALIDATION EXCEPTION --------------------------------*/

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getBindingResult().getFieldError().getDefaultMessage(),
                request.getRequestURI(),
                "Validation failed",
                LocalDateTime.now().plusMinutes(15)
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /*-------------------------------- INVALID EMAIL EXCEPTION --------------------------------*/

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<String> handleInvalidEmail(InvalidEmailException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /*-------------------------------- EMPTY EMAIL EXCEPTION --------------------------------*/

    @ExceptionHandler(EmptyEmailException.class)
    public ResponseEntity<String> handleEmptyEmail(EmptyEmailException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }


    /*-------------------------------- GENERIC EXCEPTION --------------------------------*/

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI(),
                "Internal server error",
                LocalDateTime.now().plusMinutes(15)
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
