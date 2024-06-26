package com.transferencia.controllers.handlers;

import com.amazonaws.services.sns.model.InvalidParameterException;
import com.transferencia.dto.errors.CustomError;
import com.transferencia.dto.errors.ValidationError;
import com.transferencia.services.exceptions.BusinessException;
import com.transferencia.services.exceptions.NotificacaoBacenException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.ConnectException;
import java.time.Instant;

@ControllerAdvice
public class TranferenciaControllerExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CustomError> handleBusinessException(BusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ValidationError err = new ValidationError(Instant.now(), status.value(), "Dado inválido!", request.getRequestURI());

        err.addError("Regras de negócio", e.getMessage());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<CustomError> handleConnectException(ConnectException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ValidationError err = new ValidationError(Instant.now(), status.value(), "Falhar ao conectar!", request.getRequestURI());

        err.addError("Ocorreu uma falha ao conectar com a API externa", e.getMessage());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(NotificacaoBacenException.class)
    public ResponseEntity<CustomError> handleNotificacaoBacenException(NotificacaoBacenException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ValidationError err = new ValidationError(Instant.now(), status.value(), "Falhar ao conectar!", request.getRequestURI());

        err.addError("Ocorreu uma falha ao conectar com o Basen", e.getMessage());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<CustomError> handleNotificacaoBacenException(InvalidParameterException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ValidationError err = new ValidationError(Instant.now(), status.value(), "Falha ao se comunicar com o SNS!", request.getRequestURI());

        err.addError("Falha ao enviar a transacao para o SNS", e.getMessage());

        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomError> methodArgumentNotValidation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError err = new ValidationError(Instant.now(), status.value(), "Dado inválido", request.getRequestURI());
        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }
        return ResponseEntity.status(status).body(err);
    }
}
