package com.example.OngVeterinaria.model;

import java.util.List;

public class ClienteRelatorioDTO {
    private List<ClienteModel> clientes;
    private FuncionarioModel funcionario;
    private byte[] relatorio;  // O relatório gerado em PDF

    // Getters e Setters
    public List<ClienteModel> getClientes() {
        return clientes;
    }

    public void setClientes(List<ClienteModel> clientes) {
        this.clientes = clientes;
    }

    public FuncionarioModel getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(FuncionarioModel funcionario) {
        this.funcionario = funcionario;
    }

    public byte[] getRelatorio() {
        return relatorio;
    }

    public void setRelatorio(byte[] relatorio) {
        this.relatorio = relatorio;
    }
}
