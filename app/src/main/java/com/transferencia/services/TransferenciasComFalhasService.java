package com.transferencia.services;

import com.transferencia.dto.TransferenciaRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransferenciasComFalhasService {

    private static final Logger logger = LoggerFactory.getLogger(TransferenciasComFalhasService.class);

    public void encaminharTransferenciaSNS(TransferenciaRequestDTO transferenciaRequestDTO) {

        logger.info("TransferenciasComFalhasService - encaminharTransferenciaSNS - transferenciaRequestDTO: {}", transferenciaRequestDTO);

        // SNS

    }

}
