package com.transferencia.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
@NoArgsConstructor
@Entity
@Table(name = "tb_transferencia")
public class Transferencia implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id_transferencia;
    private String idCliente;
    private Double valor;
    private String idOrigem;
    private String idDestino;
}
