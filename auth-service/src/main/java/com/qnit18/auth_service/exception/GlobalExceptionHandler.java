package com.qnit18.auth_service.exception;

import com.qnit18.auth_service.dto.response.ApiBaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiBaseResponse> handlingRuntimeException(RuntimeException exception){
        ApiBaseResponse ApiBaseResponse = new ApiBaseResponse();

        ApiBaseResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        ApiBaseResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(ApiBaseResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiBaseResponse> handlingAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiBaseResponse ApiBaseResponse = new ApiBaseResponse();

        ApiBaseResponse.setCode(errorCode.getCode());
        ApiBaseResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(ApiBaseResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiBaseResponse> handlingValidation(MethodArgumentNotValidException exception){
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e){

        }

        ApiBaseResponse ApiBaseResponse = new ApiBaseResponse();

        ApiBaseResponse.setCode(errorCode.getCode());
        ApiBaseResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(ApiBaseResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiBaseResponse> handlingAccessDenied(AccessDeniedException exception){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiBaseResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }
}
