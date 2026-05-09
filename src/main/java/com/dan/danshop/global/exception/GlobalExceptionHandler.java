package com.dan.danshop.global.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BusinessException.class)
    public ErrorResponse errorResponse(BusinessException e) {
        return ErrorResponse.from(e.getErrorCode().name(), e.getErrorCode().getMessage());
    }
}
