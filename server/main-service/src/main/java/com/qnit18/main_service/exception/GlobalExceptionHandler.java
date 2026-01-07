package com.qnit18.main_service.exception;

import com.qnit18.main_service.dto.response.ApiBaseResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiBaseResponse> handlingRuntimeException(RuntimeException exception) {
        ApiBaseResponse apiBaseResponse = new ApiBaseResponse();

        apiBaseResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiBaseResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiBaseResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiBaseResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiBaseResponse apiBaseResponse = new ApiBaseResponse();

        apiBaseResponse.setCode(errorCode.getCode());
        apiBaseResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiBaseResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiBaseResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation = exception.getBindingResult()
                    .getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

        } catch (IllegalArgumentException e) {
            log.error("Invalid key: " + enumKey);
        }

        log.info("Attributes: " + attributes);
        ApiBaseResponse apiBaseResponse = new ApiBaseResponse();

        apiBaseResponse.setCode(errorCode.getCode());
        apiBaseResponse.setMessage(Objects.nonNull(attributes)
                ? mapAttributeToMessage(attributes, errorCode.getMessage())
                : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiBaseResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiBaseResponse> handlingAccessDenied(AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiBaseResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    private String mapAttributeToMessage(Map<String, Object> attributes, String message) {
        String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}

