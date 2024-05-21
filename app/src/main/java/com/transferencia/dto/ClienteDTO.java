package com.transferencia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
public class ClienteDTO {

    private String id;
    private String nome;
    private String telefone;
    private String tipoPessoa;

}
