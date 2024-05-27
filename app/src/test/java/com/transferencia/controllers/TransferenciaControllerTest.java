package com.transferencia.controllers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transferencia.dto.ContaTransacaoDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.dto.TransferenciaResponseDTO;
import com.transferencia.services.NotificacaoService;
import com.transferencia.services.TransferenciaService;
import com.transferencia.services.ValidacaoService;
import com.transferencia.services.aws.AwsSnsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class TransferenciaControllerTest {

    @InjectMocks
    private TransferenciaController transferenciaController;

    @Mock
    private ValidacaoService validacaoService;

    @Mock
    private TransferenciaService transferenciaService;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private AwsSnsService snsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testRealizarTransferenciaSucesso() throws Exception {
        // Arrange
        ContaTransacaoDTO contaTransacaoDTO = new ContaTransacaoDTO();
        TransferenciaRequestDTO transferenciaRequestDTO = new TransferenciaRequestDTO();
        carregarDados(contaTransacaoDTO, transferenciaRequestDTO);

        when(validacaoService.validarTransferencia(any(TransferenciaRequestDTO.class))).thenReturn(null);
        when(transferenciaService.realizarTransferencia(any(TransferenciaRequestDTO.class))).thenReturn("12345");

        // Act
        ResponseEntity<?> responseEntity = transferenciaController.realizarTransferencia(transferenciaRequestDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        TransferenciaResponseDTO responseDTO = (TransferenciaResponseDTO) responseEntity.getBody();
        assertEquals("12345", responseDTO.getId_transferencia());
    }


    @Test
    public void testRealizarTransferenciaValidacaoFalha() throws Exception {
        // Arrange
        ContaTransacaoDTO contaTransacaoDTO = new ContaTransacaoDTO();
        TransferenciaRequestDTO transferenciaRequestDTO = new TransferenciaRequestDTO();
        carregarDados(contaTransacaoDTO, transferenciaRequestDTO);

        when(validacaoService.validarTransferencia(any(TransferenciaRequestDTO.class))).thenReturn("Falha durante o processo");

        // Act
        ResponseEntity<?> responseEntity = transferenciaController.realizarTransferencia(transferenciaRequestDTO);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Falha durante o processo", responseEntity.getBody());
    }

    @Test
    public void testRealizarTransferenciaNotificacaoComFalha() throws Exception {
        // Arrange
        ContaTransacaoDTO contaTransacaoDTO = new ContaTransacaoDTO();
        TransferenciaRequestDTO transferenciaRequestDTO = new TransferenciaRequestDTO();
        carregarDados(contaTransacaoDTO, transferenciaRequestDTO);

        when(validacaoService.validarTransferencia(any(TransferenciaRequestDTO.class))).thenReturn(null);
        when(transferenciaService.realizarTransferencia(any(TransferenciaRequestDTO.class))).thenReturn("12345");
        doThrow(new RuntimeException("Notification error")).when(notificacaoService).notificarBacen(any(TransferenciaRequestDTO.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            transferenciaController.realizarTransferencia(transferenciaRequestDTO);
        });
    }

    public void carregarDados(ContaTransacaoDTO contaTransacaoDTO, TransferenciaRequestDTO transferenciaRequestDTO){
        contaTransacaoDTO.setIdOrigem("123");
        contaTransacaoDTO.setIdDestino("456");
        transferenciaRequestDTO.setIdCliente("1");
        transferenciaRequestDTO.setValor(100.0);
        transferenciaRequestDTO.setConta(contaTransacaoDTO);

    }
}