package edu.example.springmvcdemo.exception.handler;

import edu.example.springmvcdemo.dto.SuccessContainerDto;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.security.exception.InvalidTokenException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import java.util.Objects;
import java.util.stream.Collectors;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class,
            BadCredentialsException.class, InvalidTokenException.class})
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(BAD_REQUEST).body(new SuccessContainerDto(false, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(BAD_REQUEST).body(new SuccessContainerDto(false, message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleException(AccessDeniedException e) {
        return ResponseEntity.status(FORBIDDEN).body(new SuccessContainerDto(false, null));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleException(EntityNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(new SuccessContainerDto(false, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception e) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new SuccessContainerDto(false, null));
    }
}
