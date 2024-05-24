package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.aws.AwsSnsService;
import com.transferencia.services.aws.MessageDTO;
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
    private AwsSnsService snsService;

    @Autowired
    private RestTemplate restTemplate;

    public ContaDTO buscarConta(TransferenciaRequestDTO transferenciaRequestDTO) throws ConnectException {

        logger.info("ContaService - buscarConta - idConta: {}", transferenciaRequestDTO.getConta().getIdOrigem());

        try {
            String url = "http://localhost:9090/contas/" + transferenciaRequestDTO.getConta().getIdOrigem();
            return restTemplate.getForObject(url, ContaDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            logger.error("Conta origem com ID {} não encontrada", transferenciaRequestDTO.getConta().getIdOrigem());
            throw new BusinessException("Conta origem com ID {"+transferenciaRequestDTO.getConta().getIdOrigem().toString()+"} não encontrada");
        } catch (Exception e) {
            logger.error("Erro ao validar cliente com ID {}: {}", transferenciaRequestDTO.getConta().getIdOrigem(), e.getMessage(), e.getCause());

            try{
                this.snsService.publicaTransferenciaFalhaTopic(transferenciaRequestDTO.toString());
            }catch (Exception ex){
                throw ex;
            }

            throw new ConnectException("Conexão recusada - A Transação será armazenada e tentaremos automaticamente em breve. Você será notificado.");
        }

    }
}
