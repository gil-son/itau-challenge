package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ContaService {

    private static final Logger logger = LoggerFactory.getLogger(ContaService.class);

    @Autowired
    private RestTemplate restTemplate;

    public ContaDTO buscarConta(String idConta) {

        logger.info("ContaService - buscarConta - idConta: {}", idConta);

        String url = "http://localhost:9090/contas/" + idConta;
        return restTemplate.getForObject(url, ContaDTO.class);
    }
}
