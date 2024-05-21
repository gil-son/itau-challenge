package com.transferencia.controllers;

import com.transferencia.dto.ClienteDTO;
import com.transferencia.services.TransferenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/transferencia")
public class TransferenciaClienteController {

    private final TransferenciaService transferenciaService;

    @Autowired
    public TransferenciaClienteController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @GetMapping
    public ClienteDTO getData() {
        return transferenciaService.getDataFromApi();
    }

}
