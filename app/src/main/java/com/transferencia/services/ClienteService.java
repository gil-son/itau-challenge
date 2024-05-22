package com.transferencia.services;


import com.transferencia.services.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.net.ConnectException;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private RestTemplate restTemplate;

    public void validarCliente(String idCliente) throws ConnectException {

        logger.info("ClienteService - validarCliente - idCliente: {}", idCliente);

        if (idCliente == null) {
            logger.error("ID do cliente é nulo ao validar o cliente");
            throw new BusinessException("ID do cliente é nulo");
        }

        String url = "http://localhost:9090/clientes/" + idCliente;

        try {
            logger.info("Validando cliente com ID: {}", idCliente);
            restTemplate.getForObject(url, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Cliente com ID {} não encontrado", idCliente);
            throw new BusinessException("Cliente com ID {"+idCliente.toString()+"} não encontrado");
        } catch (Exception e) {
            logger.error("Erro ao validar cliente com ID {}: {}", idCliente, e.getMessage(), e.getCause());
            throw new ConnectException("Conexão recusada - A Transação será armazenada e tentaremos automaticamente em breve. Você será notificado.");
        }
    }
}
