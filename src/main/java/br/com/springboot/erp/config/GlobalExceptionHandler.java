package br.com.springboot.erp.config;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    // DTO simples de erro padronizado (JSON)
    static class ErrorResponse {
        public final String timestamp = LocalDateTime.now().toString();
        public final int status;
        public final String error;
        public final String message;
        public final String path;

        ErrorResponse(HttpStatus status, String message, String path) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.path = path;
        }
    }

    // 400 - parâmetros inválidos (ex: @RequestParam inválido, @PathVariable fora de formato)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(new ErrorResponse(st, ex.getMessage(), req.getRequestURI()));
    }

    // 400 - violações de bean validation em @Validated (params) / @Valid (alguns cenários)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(new ErrorResponse(st, ex.getMessage(), req.getRequestURI()));
    }

    // 400 - corpo JSON inválido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(new ErrorResponse(st, "JSON inválido ou malformado.", req.getRequestURI()));
    }

    // 400 - @Valid em @RequestBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Parâmetros inválidos.");
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(new ErrorResponse(st, msg, req.getRequestURI()));
    }

    // 400 - bind em query/path (ex.: tipo errado)
    @ExceptionHandler({ BindException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<ErrorResponse> handleBindErrors(
            Exception ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(new ErrorResponse(st, "Parâmetro inválido.", req.getRequestURI()));
    }

    // 400 - faltou parâmetro obrigatório
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(st).body(new ErrorResponse(st, "Parâmetro obrigatório ausente: " + ex.getParameterName(), req.getRequestURI()));
    }

    // 404 - recurso não encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoSuchElementException ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(st).body(new ErrorResponse(st, ex.getMessage(), req.getRequestURI()));
    }

    // 405 / 415 (opcional)
    @ExceptionHandler({ HttpRequestMethodNotSupportedException.class, HttpMediaTypeNotSupportedException.class })
    public ResponseEntity<ErrorResponse> handleMethodOrMedia(
            Exception ex, HttpServletRequest req) {
        HttpStatus st = ex instanceof HttpRequestMethodNotSupportedException
                ? HttpStatus.METHOD_NOT_ALLOWED : HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        return ResponseEntity.status(st).body(new ErrorResponse(st, ex.getMessage(), req.getRequestURI()));
    }

    // 500 - exceções não tratadas (inclui RuntimeException)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        HttpStatus st = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(st).headers(new HttpHeaders())
                .body(new ErrorResponse(st, ex.getMessage(), req.getRequestURI()));
    }
}
