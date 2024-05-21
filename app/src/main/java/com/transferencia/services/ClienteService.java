package com.transferencia.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private RestTemplate restTemplate;

    public boolean validarCliente(String idCliente) {

        logger.info("ClienteService - validarCliente - idCliente: {}", idCliente);

        if (idCliente == null) {
            logger.error("ID do cliente é nulo ao validar o cliente");
            return false;
        }

        String url = "http://localhost:9090/clientes/" + idCliente;
        try {
            logger.info("Validando cliente com ID: {}", idCliente);
            restTemplate.getForObject(url, Void.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Cliente com ID {} não encontrado", idCliente);
            return false;
        } catch (Exception e) {
            logger.error("Erro ao validar cliente com ID {}: {}", idCliente, e.getMessage());
            return false;
        }
    }
}
