package com.example.OngVeterinaria.model;

import java.util.List;

public class DenunciaRelatorioDTO {
    private List<DenunciaModel> denuncias;
    private FuncionarioModel funcionario;
    private byte[] relatorio;

    // Getters e Setters
    public List<DenunciaModel> getDenuncias() {
        return denuncias;
    }

    public void setDenuncias(List<DenunciaModel> denuncias) {
        this.denuncias = denuncias;
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
