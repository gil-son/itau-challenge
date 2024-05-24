package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.ContaTransacaoDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class ValidacaoServiceTest {

    @InjectMocks
    private ValidacaoService validacaoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private ContaService contaService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testValidarTransferenciaSucesso() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setAtivo(true);
        contaDTO.setSaldo(200.0);
        contaDTO.setLimiteDiario(300.0);

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenReturn(contaDTO);

        // Act
        validacaoService.validarTransferencia(transferenciaRequestDTO);

        // Assert (no exceptions)
    }

    @Test
    public void testValidarTransferenciaClienteNaoExiste() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        doThrow(new BusinessException("Cliente não encontrado")).when(clienteService).validarCliente(any(TransferenciaRequestDTO.class));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            validacaoService.validarTransferencia(transferenciaRequestDTO);
        });
    }

    @Test
    public void testValidarTransferenciaContaInativa() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setAtivo(false);

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenReturn(contaDTO);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            validacaoService.validarTransferencia(transferenciaRequestDTO);
        });
    }

    @Test
    public void testValidarTransferenciaSaldoInsuficiente() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setAtivo(true);
        contaDTO.setSaldo(50.0);

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenReturn(contaDTO);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            validacaoService.validarTransferencia(transferenciaRequestDTO);
        });
    }

    @Test
    public void testValidarTransferenciaLimiteExcedido() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setAtivo(true);
        contaDTO.setSaldo(200.0);
        contaDTO.setLimiteDiario(50.0);

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenReturn(contaDTO);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            validacaoService.validarTransferencia(transferenciaRequestDTO);
        });
    }

    @Test
    public void testValidarTransferenciaHttpClientErrorException() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenThrow(HttpClientErrorException.class);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            validacaoService.validarTransferencia(transferenciaRequestDTO);
        });
    }

    @Test
    public void testValidarTransferenciaConnectException() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenThrow(ConnectException.class);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            validacaoService.validarTransferencia(transferenciaRequestDTO);
        });
    }

    private TransferenciaRequestDTO criarTransferenciaRequestDTO() {
        ContaTransacaoDTO contaTransacaoDTO = new ContaTransacaoDTO();
        contaTransacaoDTO.setIdOrigem("123");
        contaTransacaoDTO.setIdDestino("456");

        TransferenciaRequestDTO transferenciaRequestDTO = new TransferenciaRequestDTO();
        transferenciaRequestDTO.setIdCliente("1");
        transferenciaRequestDTO.setValor(100.0);
        transferenciaRequestDTO.setConta(contaTransacaoDTO);

        return transferenciaRequestDTO;
    }
}
