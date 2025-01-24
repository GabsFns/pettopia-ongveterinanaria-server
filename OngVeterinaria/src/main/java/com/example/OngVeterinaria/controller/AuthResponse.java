package com.example.OngVeterinaria.controller;

import com.example.OngVeterinaria.DTO.ClienteDTO;
import com.example.OngVeterinaria.model.ClienteModel;

public class AuthResponse {
    private String token;
    private ClienteDTO cliente;

    private ClienteModel clienteModel;

    public AuthResponse(String token, ClienteModel clienteModel) {
        this.token = token;
        this.clienteModel = clienteModel;
    }
    public AuthResponse(String token, ClienteDTO cliente) {
        this.token = token;
        this.cliente = cliente;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ClienteDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteDTO cliente) {
        this.cliente = cliente;
    }
    public ClienteModel getClienteModel() {
        return clienteModel;
    }

    public void setClienteModel(ClienteModel clienteModel) {
        this.clienteModel = clienteModel;
    }
}
