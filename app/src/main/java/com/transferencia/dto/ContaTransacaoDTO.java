package com.transferencia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ContaTransacao {

    private String idOrigem;
    private String idDestino;
}
