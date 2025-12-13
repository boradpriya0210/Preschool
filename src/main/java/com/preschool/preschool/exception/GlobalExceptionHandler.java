package com.preschool.preschool.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Helper Method to Determine if Request is for API
    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String uri = request.getRequestURI();
        String xRequestedWith = request.getHeader("X-Requested-With");

        // Check for specific API path prefix
        if (uri.startsWith("/api/")) {
            return true;
        }

        // Check for AJAX headers
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            return true;
        }

        // Check Accept header
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return true;
        }

        return false;
    }

    // Handle Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Validation Error",
                    errorMessage,
                    request.getRequestURI());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("status", HttpStatus.BAD_REQUEST.value());
            mav.addObject("error", "Validation Error");
            mav.addObject("message", errorMessage);
            mav.addObject("path", request.getRequestURI());
            return mav;
        }
    }

    // Handle Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    ex.getMessage(),
                    request.getRequestURI());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("status", HttpStatus.NOT_FOUND.value());
            mav.addObject("error", "Resource Not Found");
            mav.addObject("message", ex.getMessage());
            mav.addObject("path", request.getRequestURI());
            return mav;
        }
    }

    // Handle Authentication & Authorization Errors
    @ExceptionHandler({ AccessDeniedException.class, AuthenticationException.class })
    public Object handleAuthExceptions(RuntimeException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String errorLabel = "Access Denied";

        if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            errorLabel = "Unauthorized";
        }

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = new ErrorResponse(
                    status.value(),
                    errorLabel,
                    ex.getMessage(),
                    request.getRequestURI());
            return new ResponseEntity<>(errorResponse, status);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("status", status.value());
            mav.addObject("error", errorLabel);
            mav.addObject("message", ex.getMessage());
            mav.addObject("path", request.getRequestURI());
            return mav;
        }
    }

    // Handle Malformed JSON / Request Body
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Object handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        if (isApiRequest(request)) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "Malformed JSON request or missing body: " + ex.getMessage(),
                    request.getRequestURI());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("status", HttpStatus.BAD_REQUEST.value());
            mav.addObject("error", "Bad Request");
            mav.addObject("message", "Invalid request data");
            mav.addObject("path", request.getRequestURI());
            return mav;
        }
    }

    // Handle Generic Exceptions
    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception ex, HttpServletRequest request) {
        // Log the exception (using System.err for now, better to use Logger)
        System.err.println("Global Exception Handler caught: " + ex.getMessage());
        ex.printStackTrace();

        if (isApiRequest(request)) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "An unexpected error occurred", // Hide stack trace for security
                    request.getRequestURI());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            ModelAndView mav = new ModelAndView("error");
            mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            mav.addObject("error", "Internal Server Error");
            mav.addObject("message", "An unexpected error occurred. Please try again later.");
            mav.addObject("path", request.getRequestURI());
            return mav;
        }
    }
}
