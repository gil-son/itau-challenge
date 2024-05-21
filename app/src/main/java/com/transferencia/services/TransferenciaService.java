package com.transferencia.services;

import com.transferencia.dto.ContaDTO;
import com.transferencia.dto.TransferenciaRequestDTO;
import com.transferencia.entities.Transferencia;
import com.transferencia.repositories.TransferenciaRepository;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
public class TransferenciaService {

    private static final Logger logger = LoggerFactory.getLogger(TransferenciaService.class);

    @Autowired
    private ContaService contaService;

    @Autowired
    private SaldoService saldoService;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    public String realizarTransferencia(TransferenciaRequestDTO transferenciaRequestDTO) {

        logger.info("TransferenciaService - realizarTransferencia - DTO: {}", transferenciaRequestDTO);

        // Buscar dados da conta origem
        ContaDTO contaOrigem = contaService.buscarConta(transferenciaRequestDTO.getConta().getIdOrigem());

        // Atualizar saldo da conta origem
        contaOrigem.setSaldo(contaOrigem.getSaldo() - transferenciaRequestDTO.getValor());

        logger.info("TransferenciaService - realizarTransferencia - Ajuste saldo: {}", contaOrigem.getSaldo());

        Transferencia entity = new Transferencia();
        copyDtoToEntity(transferenciaRequestDTO, entity);

        saldoService.atualizarSaldos(transferenciaRequestDTO);

        transferenciaRepository.save(entity);

        return entity.getId_transferencia();
    }

    private void copyDtoToEntity(@NotNull TransferenciaRequestDTO dto, @NotNull Transferencia entity){
        entity.setId_transferencia(UUID.randomUUID().toString());
        entity.setIdCliente(dto.getIdCliente());
        entity.setValor(dto.getValor());
        entity.setIdOrigem(dto.getConta().getIdOrigem());
        entity.setIdDestino(dto.getConta().getIdDestino());

    }
}
