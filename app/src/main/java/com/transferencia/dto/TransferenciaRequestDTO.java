package com.transferencia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class TransferenciaRequestDTO {

    private String idCliente;
    private Double valor;
    private ContaTransacaoDTO conta;
}
