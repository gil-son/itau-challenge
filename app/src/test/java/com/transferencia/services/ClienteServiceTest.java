package com.transferencia.services;

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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AwsSnsService snsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testValidarClienteSucesso() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        // Act
        clienteService.validarCliente(transferenciaRequestDTO);

        // Assert
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Void.class));
    }

    @Test
    public void testValidarClienteIdClienteNulo() {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = new TransferenciaRequestDTO();
        transferenciaRequestDTO.setIdCliente(null);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            clienteService.validarCliente(transferenciaRequestDTO);
        });

        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testValidarClienteClienteNaoEncontrado() {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        HttpClientErrorException notFoundException = HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", null, null, null);

        doThrow(notFoundException).when(restTemplate).getForObject(anyString(), eq(Void.class));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            clienteService.validarCliente(transferenciaRequestDTO);
        });

        verify(restTemplate, times(1)).getForObject(anyString(), eq(Void.class));
    }

    @Test
    public void testValidarClienteConexaoRecusada() throws Exception {
        // Arrange
        TransferenciaRequestDTO transferenciaRequestDTO = criarTransferenciaRequestDTO();

        doThrow(new ResourceAccessException("Conexão recusada")).when(restTemplate).getForObject(anyString(), eq(Void.class));

        // Act & Assert
        assertThrows(ConnectException.class, () -> {
            clienteService.validarCliente(transferenciaRequestDTO);
        });

        verify(restTemplate, times(1)).getForObject(anyString(), eq(Void.class));
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
