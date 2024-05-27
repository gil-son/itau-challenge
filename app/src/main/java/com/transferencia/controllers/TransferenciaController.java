package com.transferencia.controllers;

import com.transferencia.dto.TransferenciaResponseDTO;
import com.transferencia.services.aws.AwsSnsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.NotificacaoService;
import com.transferencia.services.TransferenciaService;
import com.transferencia.services.ValidacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.ConnectException;

@RestController
@RequestMapping(value = "/transferencia")
public class TransferenciaController {

    private static final Logger logger = LoggerFactory.getLogger(TransferenciaController.class);
    @Autowired
    private ValidacaoService validacaoService;

    @Autowired
    private TransferenciaService transferenciaService;

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private AwsSnsService snsService;

    @PostMapping
    public ResponseEntity<?> realizarTransferencia(@Valid @RequestBody TransferenciaRequestDTO transferenciaRequestDTO) throws ConnectException {

        logger.info("TransferenciaController - realizarTransferencia - DTO: {}", transferenciaRequestDTO);
        // Validar a transferência
        String validacaoErro = validacaoService.validarTransferencia(transferenciaRequestDTO);
        if (validacaoErro != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validacaoErro);
        }

        // Realizar a transferência
        String idTransferencia = transferenciaService.realizarTransferencia(transferenciaRequestDTO);

        // Notificar o BACEN
        notificacaoService.notificarBacen(transferenciaRequestDTO);

        // Criar o objeto de resposta
        TransferenciaResponseDTO responseDTO = new TransferenciaResponseDTO(idTransferencia);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
