package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SaldoService {

    @Autowired
    private RestTemplate restTemplate;

    public ContaDTO buscarConta(String idConta) {
        String url = "http://localhost:9090/contas/" + idConta;
        return restTemplate.getForObject(url, ContaDTO.class);
    }
}
