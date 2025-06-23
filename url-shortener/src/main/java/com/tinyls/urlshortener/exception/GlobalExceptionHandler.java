package com.tinyls.urlshortener.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler for the application.
 * This class provides centralized exception handling across all controllers.
 * 
 * It handles various types of exceptions and converts them into standardized
 * API error responses using the ApiError class.
 * 
 * Handled exceptions include:
 * - Validation errors
 * - Authentication errors
 * - Authorization errors
 * - Resource not found errors
 * - General runtime errors
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

        /**
         * Handles validation errors from @Valid annotations.
         * Converts validation errors into a standardized API error response.
         */
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpHeaders headers,
                        HttpStatusCode status,
                        WebRequest request) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Validation error")
                                .debugMessage("One or more fields failed validation")
                                .build();

                List<ApiValidationError> validationErrors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(this::mapToApiValidationError)
                                .collect(Collectors.toList());
                apiError.getSubErrors().addAll(validationErrors);
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles constraint violation exceptions.
         * These occur when @Validated annotations fail validation.
         */
        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<Object> handleConstraintViolationException(
                        ConstraintViolationException ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Validation error")
                                .debugMessage("One or more fields failed validation")
                                .build();

                List<ApiValidationError> validationErrors = ex.getConstraintViolations()
                                .stream()
                                .map(violation -> new ApiValidationError(
                                                violation.getRootBeanClass().getSimpleName(),
                                                violation.getPropertyPath().toString(),
                                                violation.getInvalidValue(),
                                                violation.getMessage()))
                                .collect(Collectors.toList());
                apiError.getSubErrors().addAll(validationErrors);
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles resource not found exceptions.
         * These occur when a requested entity doesn't exist.
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(ex.getMessage())
                                .debugMessage("The requested resource was not found")
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
        }

        /**
         * Handles authentication exceptions.
         * These occur when user credentials are invalid.
         */
        @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
        public ResponseEntity<Object> handleAuthenticationException(Exception ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Authentication failed")
                                .debugMessage(ex.getMessage())
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles authorization exceptions.
         * These occur when a user lacks required permissions.
         */
        @ExceptionHandler({ UnauthorizedException.class, AccessDeniedException.class })
        public ResponseEntity<Object> handleUnauthorizedException(Exception ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message("Access denied")
                                .debugMessage(ex.getMessage())
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
        }

        /**
         * Handles email already exists exceptions.
         * These occur during user registration with duplicate emails.
         */
        @ExceptionHandler(EmailAlreadyExistsException.class)
        public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message(ex.getMessage())
                                .debugMessage("Email already exists")
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
        }

        /**
         * Handles password validation exceptions.
         * These occur when a password fails to meet strength requirements.
         */
        @ExceptionHandler(PasswordValidationException.class)
        public ResponseEntity<Object> handlePasswordValidationException(PasswordValidationException ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message("Password validation failed")
                                .debugMessage(ex.getMessage())
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles incorrect password exceptions.
         * These occur when a user attempts to update their password with an incorrect
         * current password.
         */
        @ExceptionHandler(IncorrectPasswordException.class)
        public ResponseEntity<Object> handleIncorrectPasswordException(IncorrectPasswordException ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(ex.getMessage())
                                .debugMessage("The current password provided is incorrect")
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles all other unhandled exceptions.
         * Provides a generic error response for unexpected errors.
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Object> handleAllUncaughtException(Exception ex) {
                ApiError apiError = ApiError.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message("An unexpected error occurred")
                                .debugMessage(ex.getMessage())
                                .build();
                return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        /**
         * Maps a Spring FieldError to an ApiValidationError.
         * 
         * @param fieldError the Spring FieldError to convert
         * @return the corresponding ApiValidationError
         */
        private ApiValidationError mapToApiValidationError(FieldError fieldError) {
                return new ApiValidationError(
                                fieldError.getObjectName(),
                                fieldError.getField(),
                                fieldError.getRejectedValue(),
                                fieldError.getDefaultMessage());
        }
}