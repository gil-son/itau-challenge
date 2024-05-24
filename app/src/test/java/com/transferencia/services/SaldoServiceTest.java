package com.transferencia.services;

import com.transferencia.dto.ContaTransacaoDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.aws.AwsSnsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SaldoServiceTest {

    @InjectMocks
    private SaldoService saldoService;

    @Mock
    private AwsSnsService snsService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAtualizarSaldosSucesso() {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        // Act
        saldoService.atualizarSaldos(transferenciaRequestDTO);

        // Assert
        verify(restTemplate, times(1)).put(anyString(), eq(transferenciaRequestDTO), eq(Void.class));
        verify(snsService, times(0)).publicaTransferenciaFalhaTopic(anyString());
    }

    @Test
    public void testAtualizarSaldosHttpClientErrorException() {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();
        HttpClientErrorException clientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST);

        doThrow(clientErrorException).when(restTemplate).put(anyString(), eq(transferenciaRequestDTO), eq(Void.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            saldoService.atualizarSaldos(transferenciaRequestDTO);
        });

        assertEquals("Erro ao atualizar os saldos das contas. A transação será retomada em breve. Não teve desconto de saldo da conta de origem por hora", exception.getMessage());
        verify(restTemplate, times(1)).put(anyString(), eq(transferenciaRequestDTO), eq(Void.class));
        verify(snsService, times(1)).publicaTransferenciaFalhaTopic(anyString());
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

