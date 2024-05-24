package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.ContaTransacaoDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.aws.AwsSnsService;
import com.transferencia.services.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.net.ConnectException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ContaServiceTest {

    @InjectMocks
    private ContaService contaService;

    @Mock
    private AwsSnsService snsService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBuscarContaSucesso() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();
        ContaDTO contaDTO = new ContaDTO();
        contaDTO.setId("123");
        contaDTO.setAtivo(true);
        contaDTO.setSaldo(500.0);
        contaDTO.setLimiteDiario(1000.0);

        when(restTemplate.getForObject(anyString(), eq(ContaDTO.class))).thenReturn(contaDTO);

        // Act
        ContaDTO result = contaService.buscarConta(transferenciaRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals("123", result.getId());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ContaDTO.class));
    }

    @Test
    public void testBuscarContaNaoEncontrada() {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        HttpClientErrorException notFoundException = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);

        doThrow(notFoundException).when(restTemplate).getForObject(anyString(), eq(ContaDTO.class));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            contaService.buscarConta(transferenciaRequestDTO);
        });

        verify(restTemplate, times(1)).getForObject(anyString(), eq(ContaDTO.class));
    }

    @Test
    public void testBuscarContaConexaoRecusada() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        doThrow(new ResourceAccessException("Conexão recusada")).when(restTemplate).getForObject(anyString(), eq(ContaDTO.class));

        // Act & Assert
        assertThrows(ConnectException.class, () -> {
            contaService.buscarConta(transferenciaRequestDTO);
        });

        verify(restTemplate, times(1)).getForObject(anyString(), eq(ContaDTO.class));
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
