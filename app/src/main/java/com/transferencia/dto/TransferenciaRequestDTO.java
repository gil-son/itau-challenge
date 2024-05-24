package com.transferencia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONObject;

@Setter
@Getter
@NoArgsConstructor
public class TransferenciaRequestDTO {

    private String idCliente;
    private Double valor;
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
