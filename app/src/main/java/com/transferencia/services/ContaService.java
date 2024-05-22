package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.services.exceptions.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;

@Service
public class ContaService {

    private static final Logger logger = LoggerFactory.getLogger(ContaService.class);

    @Autowired
    private RestTemplate restTemplate;

    public ContaDTO buscarConta(String idConta) throws ConnectException {

        logger.info("ContaService - buscarConta - idConta: {}", idConta);

        try {
            String url = "http://localhost:9090/contas/" + idConta;
            return restTemplate.getForObject(url, ContaDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Conta origem com ID {} não encontrada", idConta);
            throw new BusinessException("Conta origem com ID {"+idConta.toString()+"} não encontrada");
        } catch (Exception e) {
            logger.error("Erro ao validar cliente com ID {}: {}", idConta, e.getMessage(), e.getCause());
            throw new ConnectException("Conexão recusada - A Transação será armazenada e tentaremos automaticamente em breve. Você será notificado.");
        }

    }
}
