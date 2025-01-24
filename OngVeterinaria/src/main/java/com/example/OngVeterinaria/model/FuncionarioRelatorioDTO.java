package com.example.OngVeterinaria.model;

import java.util.List;

public class FuncionarioRelatorioDTO {

    // Lista de funcionários que serão incluídos no relatório
    private List<FuncionarioModel> funcionarios;

    // Administrador que está gerando o relatório
    private FuncionarioModel administrador;

    // Relatório em PDF, representado como um array de bytes
    private byte[] relatorio;

    // Getters e Setters
    public List<FuncionarioModel> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<FuncionarioModel> funcionarios) {
        this.funcionarios = funcionarios;
    }

    public FuncionarioModel getAdministrador() {
        return administrador;
    }

    public void setAdministrador(FuncionarioModel administrador) {
        this.administrador = administrador;
    }

    public byte[] getRelatorio() {
        return relatorio;
    }

    public void setRelatorio(byte[] relatorio) {
        this.relatorio = relatorio;
    }
}
