package com.dan.danshop.global.exception;

import lombok.Getter;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Getter
@RestControllerAdvice
public class GlobalExceptionHandler {

    //TODO
//    @ExceptionHandler(BusinessException.class)
//    public ErrorResponse errorResponse() {
//
//    }
}
