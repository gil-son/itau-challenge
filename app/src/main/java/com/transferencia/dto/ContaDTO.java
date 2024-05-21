package com.transferencia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ContaDTO {

    private String id;
    private Double saldo;
    private Double limiteDiario;
    private Boolean ativo;

}
