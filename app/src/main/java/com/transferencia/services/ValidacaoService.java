package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class ValidacaoService {

    private static final Logger logger = LoggerFactory.getLogger(ValidacaoService.class);

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ContaService contaService;

    public String validarTransferencia(TransferenciaRequestDTO transferenciaRequestDTO) {

        logger.info("ValidacaoService - validarTransferencia -  transferenciaRequestDTO: {}", transferenciaRequestDTO);

        try{
            // Validar se o cliente que vai receber a transferência existe
            boolean clienteExiste = clienteService.validarCliente(transferenciaRequestDTO.getIdCliente());
            if (!clienteExiste) {
                return "Cliente destinatário não encontrado.";
            }

        }catch (HttpClientErrorException e){
            logger.error("Erro ao buscar dados da conta origem: {}", e.getMessage());
            return "Erro ao buscar dados do cliente.";
        }

        logger.info("validarTransferencia -  transferenciaRequestDTO - getIdOrigem : {}", transferenciaRequestDTO.getConta().getIdOrigem());

        // Buscar dados da conta origem
        try{
            ContaDTO contaOrigem = contaService.buscarConta(transferenciaRequestDTO.getConta().getIdOrigem());
            logger.info("validarTransferencia -  transferenciaRequestDTO - contaOrigem.isAtiva : {}", contaOrigem.getAtivo());

            if (contaOrigem == null || !contaOrigem.getAtivo()) {
                return "Conta origem inválida ou inativa.";
            }

            // Validar saldo disponível
            if (contaOrigem.getSaldo() < transferenciaRequestDTO.getValor()) {
                return "Saldo insuficiente.";
            }

            // Validar limite diário
            if (contaOrigem.getLimiteDiario() < transferenciaRequestDTO.getValor()) {
                return "Limite diário excedido.";
            }

        }catch (HttpClientErrorException e){
            logger.error("Erro ao buscar dados da conta origem: {}", e.getMessage());
            return "Erro ao buscar dados da conta origem.";
        }

        return null;
    }
}
