package com.transferencia.services;


import com.amazonaws.services.sns.model.InvalidParameterException;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.aws.AwsSnsService;
import com.transferencia.services.aws.MessageDTO;
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

    @Autowired
    private AwsSnsService snsService;

    public void validarCliente(TransferenciaRequestDTO transferenciaRequestDTO) throws ConnectException {

        logger.info("ClienteService - validarCliente - idCliente: {}", transferenciaRequestDTO.getIdCliente());

        if (transferenciaRequestDTO.getIdCliente() == null) {
            logger.error("ID do cliente é nulo ao validar o cliente");
            throw new BusinessException("ID do cliente é nulo");
        }

        String url = "http://localhost:9090/clientes/" + transferenciaRequestDTO.getIdCliente();

        try {
            logger.info("Validando cliente com ID: {}", transferenciaRequestDTO.getIdCliente());
            restTemplate.getForObject(url, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Cliente com ID {} não encontrado", transferenciaRequestDTO.getIdCliente());
            throw new BusinessException("Cliente com ID {"+transferenciaRequestDTO.getIdCliente().toString()+"} não encontrado");
        } catch (Exception e) {
            logger.error("Erro ao validar cliente com ID {}: {}", transferenciaRequestDTO.getIdCliente(), e.getMessage(), e.getCause());

            try{
                this.snsService.publicaTransferenciaFalhaTopic(transferenciaRequestDTO.toString()); // ou publishToBasenFalhaTopic, dependendo do seu caso
            } catch (Exception ex){
                throw ex;
            }

            throw new ConnectException("Conexão recusada - A Transação será armazenada e tentaremos automaticamente em breve. Você será notificado.");
        }
    }
}
