package com.transferencia.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.json.JSONObject;

@Setter
@Getter
@NoArgsConstructor
public class TransferenciaRequestDTO {

    @NotNull(message = "O id do cliente é requirido!")
    @NotBlank(message = "O id do cliente é requirido!")
    private String idCliente;

    @Positive(message = "O valor deve de transferência deve ser maior que zero")
    private Double valor;

    @NotNull(message = "Os dados das contas são requirídos!")
    private ContaTransacaoDTO conta;

    @Override
    public String toString(){

        JSONObject jsonInterno = new JSONObject();
        jsonInterno.put("idOrigem", this.conta.getIdOrigem());
        jsonInterno.put("idDestino", this.conta.getIdDestino());

        JSONObject jsonExterno = new JSONObject();
        jsonExterno.put("idCliente", this.idCliente);
        jsonExterno.put("valor", this.valor);
        jsonExterno.put("conta", jsonInterno);

        return jsonExterno.toString();
    }


}
