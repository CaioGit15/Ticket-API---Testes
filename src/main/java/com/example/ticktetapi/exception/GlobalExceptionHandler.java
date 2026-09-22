package com.example.ticktetapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNaoEncontrado(RecursoNaoEncontradoException ex,
                                                             HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraNegocio(RegraNegocioException ex,
                                                            HttpServletRequest request) {
        return construir(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(MethodArgumentNotValidException ex,
                                                         HttpServletRequest request) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Erro de validação", request, detalhes);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex, HttpServletRequest request) {
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno: " + ex.getMessage(), request, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensagem,
                                                    HttpServletRequest request, List<String> detalhes) {
        ErrorResponse erro = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erro(status.getReasonPhrase())
                .mensagem(mensagem)
                .path(request.getRequestURI())
                .detalhes(detalhes)
                .build();
        return ResponseEntity.status(status).body(erro);
    }
}
