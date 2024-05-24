package com.transferencia.services;

import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.services.aws.AwsSnsService;
import com.transferencia.services.exceptions.NotificacaoBacenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);

    @Autowired
    private AwsSnsService snsService;

    @Autowired
    private RestTemplate restTemplate;

    public boolean notificarBacen(TransferenciaRequestDTO transferenciaRequestDTO) throws ConnectException {

        logger.info("NotificacaoService - notificarBacen - transferenciaRequestDTO: {}", transferenciaRequestDTO);

        String url = "http://localhost:9090/notificacoes";
        try {
            restTemplate.postForEntity(url, transferenciaRequestDTO, Void.class);
            return true;
        } catch (HttpClientErrorException.TooManyRequests e) {

            this.snsService.publishToBasenFalhaTopic(transferenciaRequestDTO.toString());

            throw new NotificacaoBacenException("Falha ao se comunicar com o BASEN - A Transação foi processada. Em breve quando o BASEN estiver disponível, receberá o registro.");
        } catch (Exception e) {

            this.snsService.publishToBasenFalhaTopic(transferenciaRequestDTO.toString());

            throw new ConnectException("Conexão recusada - A Transação foi processada. Em breve quando o BASEN estiver disponível, receberá o registro.");
        }
    }
}