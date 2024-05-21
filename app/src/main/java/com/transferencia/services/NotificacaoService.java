package com.transferencia.services;

import com.transferencia.dto.TransferenciaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);

    @Autowired
    private RestTemplate restTemplate;

    public boolean notificarBacen(TransferenciaRequestDTO transferenciaRequestDTO) {

        logger.info("NotificacaoService - notificarBacen - transferenciaRequestDTO: {}", transferenciaRequestDTO);

        String url = "http://localhost:9090/notificacoes";
        try {
            restTemplate.postForEntity(url, transferenciaRequestDTO, Void.class);
            return true;
        } catch (HttpClientErrorException.TooManyRequests e) {
            return false;
        }
    }
}
