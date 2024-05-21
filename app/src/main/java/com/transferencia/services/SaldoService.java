package com.transferencia.services;

import com.transferencia.dto.TransferenciaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class SaldoService {

    private static final Logger logger = LoggerFactory.getLogger(SaldoService.class);

    @Autowired
    private RestTemplate restTemplate;

    public void atualizarSaldos(TransferenciaRequestDTO transferenciaRequestDTO) {

        logger.info("TransferenciaRequestDTO - atualizarSaldos - transferenciaRequestDTO: {}", transferenciaRequestDTO);

        String url = "http://localhost:9090/contas/saldos";
        try {
            restTemplate.put(url, transferenciaRequestDTO, Void.class);
        } catch (HttpClientErrorException e) {
            // Log de erro ou tratamento adicional, se necessário
            throw new RuntimeException("Erro ao atualizar os saldos das contas", e);
        }
    }
}
