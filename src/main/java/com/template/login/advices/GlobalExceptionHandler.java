package com.template.login.advices;

import com.template.login.exceptions.ResourceAlreadyExistsException;
import com.template.login.exceptions.ResourceNotFoundException;
import com.template.login.exceptions.UnauthorizedAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<APIResponse<?>> conflictHandler(ResourceAlreadyExistsException ex) {
        APIError err = APIError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.CONFLICT)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new APIResponse<>(err));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<?>> inputNotValidHandler(MethodArgumentNotValidException ex) {
        APIError err = APIError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new APIResponse<>(err));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<?>> resourceNotFoundException(Exception ex) {
        APIError err = APIError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new APIResponse<>(err));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<APIResponse<?>> unauthorizedAccessException(Exception ex) {
        APIError err = APIError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED)
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new APIResponse<>(err));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<?>> handler(Exception ex) {
        APIError err = APIError.builder()
                .message(ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new APIResponse<>(err));
    }
}
