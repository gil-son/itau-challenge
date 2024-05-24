package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.dto.ContaTransacaoDTO;
import com.transferencia.entities.Transferencia;
import com.transferencia.repositories.TransferenciaRepository;
import com.transferencia.services.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransferenciaServiceTest {

    @InjectMocks
    private TransferenciaService transferenciaService;

    @Mock
    private ContaService contaService;

    @Mock
    private SaldoService saldoService;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRealizarTransferenciaSucceso() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();
        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setId("123");
        contaDTO.setAtivo(true);
        contaDTO.setSaldo(500.0);
        contaDTO.setLimiteDiario(1000.0);

        when(contaService.buscarConta(any(TransferenciaRequestDTO.class))).thenReturn(contaDTO);
        doNothing().when(saldoService).atualizarSaldos(any(TransferenciaRequestDTO.class));
        when(transferenciaRepository.save(any(Transferencia.class))).thenAnswer(invocation -> {
            Transferencia transferencia = invocation.getArgument(0);
            transferencia.setId_transferencia("generated-id");
            return transferencia;
        });

        // Act
        String result = transferenciaService.realizarTransferencia(transferenciaRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("generated-id", result);
        assertEquals(400.0, contaDTO.getSaldo());
        verify(contaService, times(1)).buscarConta(any(TransferenciaRequestDTO.class));
        verify(saldoService, times(1)).atualizarSaldos(any(TransferenciaRequestDTO.class));
        verify(transferenciaRepository, times(1)).save(any(Transferencia.class));
    }

    @Test
    public void testRealizarTransferenciaContaNaoEncontrada() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();
        doThrow(new BusinessException("Conta origem não encontrada")).when(contaService).buscarConta(any(TransferenciaRequestDTO.class));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            transferenciaService.realizarTransferencia(transferenciaRequestDTO);
        });

        verify(contaService, times(1)).buscarConta(any(TransferenciaRequestDTO.class));
        verify(saldoService, times(0)).atualizarSaldos(any(TransferenciaRequestDTO.class));
        verify(transferenciaRepository, times(0)).save(any(Transferencia.class));
    }

    @Test
    public void testRealizarTransferenciaConexaoRecusada() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();
        doThrow(new ConnectException("Conexão recusada")).when(contaService).buscarConta(any(TransferenciaRequestDTO.class));

        // Act & Assert
        assertThrows(ConnectException.class, () -> {
            transferenciaService.realizarTransferencia(transferenciaRequestDTO);
        });

        verify(contaService, times(1)).buscarConta(any(TransferenciaRequestDTO.class));
        verify(saldoService, times(0)).atualizarSaldos(any(TransferenciaRequestDTO.class));
        verify(transferenciaRepository, times(0)).save(any(Transferencia.class));
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

