package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import com.transferencia.services.exceptions.BusinessException;

import java.net.ConnectException;

@Service
public class ValidacaoService {

    private static final Logger logger = LoggerFactory.getLogger(ValidacaoService.class);

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ContaService contaService;

    public String validarTransferencia(TransferenciaRequestDTO transferenciaRequestDTO) throws ConnectException {

        logger.info("ValidacaoService - validarTransferencia -  transferenciaRequestDTO: {}", transferenciaRequestDTO);

        // Validar se o cliente que vai receber a transferência existe
        clienteService.validarCliente(transferenciaRequestDTO);

        logger.info("validarTransferencia -  transferenciaRequestDTO - getIdOrigem : {}", transferenciaRequestDTO.getConta().getIdOrigem());

        // Buscar dados da conta origem
        try{
            ContaDTO contaOrigem = contaService.buscarConta(transferenciaRequestDTO);
            logger.info("validarTransferencia -  transferenciaRequestDTO - contaOrigem.isAtiva : {}", contaOrigem.getAtivo());

            if (contaOrigem == null || !contaOrigem.getAtivo()) {
                throw new BusinessException("Conta origem inválida ou inativa");
            }

            // Validar saldo disponível
            if (contaOrigem.getSaldo() < transferenciaRequestDTO.getValor()) {
                throw new BusinessException("Saldo insuficiente");
            }

            // Validar limite diário
            if (contaOrigem.getLimiteDiario() < transferenciaRequestDTO.getValor()) {
                throw new BusinessException("Limite diário excedido");
            }

        }catch (HttpClientErrorException e){
            logger.error("Erro ao buscar dados da conta origem: {}", e.getMessage());
            throw new BusinessException( "Erro ao buscar dados da conta origem.");
        } catch (ConnectException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
