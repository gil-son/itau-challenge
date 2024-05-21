package com.transferencia.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "tb_club")
public class TransferenciaRequest {

    private String id_transferencia;
    private String idCliente;
    private Double valor;
    private ContaTransacao conta;
}
