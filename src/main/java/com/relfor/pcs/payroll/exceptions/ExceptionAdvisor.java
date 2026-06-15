package com.relfor.pcs.payroll.exceptions;


import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityNotFoundException;
import java.lang.IllegalStateException;
import java.text.ParseException;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionAdvisor extends ResponseEntityExceptionHandler {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException methodArgumentNotValidException,
    		HttpHeaders headers, org.springframework.http.HttpStatusCode status, WebRequest request) {
        String ERROR_MESSAGE = methodArgumentNotValidException.getMessage();
        try {
            ERROR_MESSAGE = methodArgumentNotValidException.getBindingResult().getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));
        } catch (Exception e) {
            logger.error("Error constructing error message", e);
            logger.error(e.getClass().getName(),e);
        }

        logger.error(ERROR_MESSAGE, methodArgumentNotValidException);
        ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.BAD_REQUEST, ERROR_MESSAGE);
        return handleExceptionInternal(methodArgumentNotValidException, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
    }
	
	
	@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest webRequest) {
        final String ERROR_MESSAGE = exception.getMessage();
        logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
        ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.BAD_REQUEST, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }
	
	@ExceptionHandler(java.lang.IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException exception, WebRequest webRequest) {
        final String ERROR_MESSAGE = exception.getMessage();
        logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
        ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.BAD_REQUEST, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);
    }
	
	
	@ExceptionHandler({ ConstraintViolationException.class })
    public ResponseEntity<Object> handleBadRequest(final ConstraintViolationException exception, final WebRequest request) {
		final String ERROR_MESSAGE = exception.getMessage();
		logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
		ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.BAD_REQUEST, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
	
	
	@ExceptionHandler({ DataIntegrityViolationException.class })
    public ResponseEntity<Object> handleBadRequest(final DataIntegrityViolationException exception, final WebRequest request) {
		final String ERROR_MESSAGE = exception.getMessage();
		logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
		ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.BAD_REQUEST, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
	
	
	@ExceptionHandler(value = { EntityNotFoundException.class })
    public ResponseEntity<Object> handleNotFound(final RuntimeException exception, final WebRequest request) {
		final String ERROR_MESSAGE = exception.getMessage();
		logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
		ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.NOT_FOUND, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
	
	@ExceptionHandler(value = { ResourceNotFoundException.class })
    public ResponseEntity<Object> handleResourceNotFound(final RuntimeException exception, final WebRequest request) {
		final String ERROR_MESSAGE = exception.getMessage();
		logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
		ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.NOT_FOUND, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
	
	
	 @ExceptionHandler({ NullPointerException.class })
	 public ResponseEntity<Object> handleInternal(final RuntimeException exception, final WebRequest request) {
		 final String ERROR_MESSAGE = exception.getMessage();
		 logger.error(ERROR_MESSAGE, exception);
         logger.error(exception.getClass().getName(),exception);
		 ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_MESSAGE);
		 return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
	
	@ExceptionHandler(ParseException.class) 
	public ResponseEntity<Object> handleParseException(Exception exception, final WebRequest request) {
		final String ERROR_MESSAGE = exception.getMessage();
		logger.error(ERROR_MESSAGE, exception);
		ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_MESSAGE);
		return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
	
	@ExceptionHandler({ NumberFormatException.class })
	 public ResponseEntity<Object> handleNumberFormatException(final RuntimeException exception, final WebRequest request) {
		 final String ERROR_MESSAGE = exception.getMessage();
		 logger.error(ERROR_MESSAGE, exception);
         logger.error(exception.getClass().getName(),exception);
		 ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_MESSAGE);
		 return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
	}
	 
	@ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAnyException(Exception exception, WebRequest webRequest) {
        final String ERROR_MESSAGE = "An unexpected error occurred";
        logger.error(ERROR_MESSAGE, exception);
        logger.error(exception.getClass().getName(),exception);
        ApiExceptionResponse errorResponse = getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_MESSAGE);
        return handleExceptionInternal(exception, errorResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }
	
	
	private ApiExceptionResponse getErrorResponse(HttpStatus status, String errorMessage) {
        if (ObjectUtils.isEmpty(errorMessage)) {
            errorMessage = "An unexpected error occurred";
        }
        return new ApiExceptionResponse(status.value(), status.name(), errorMessage);
    }


	

}