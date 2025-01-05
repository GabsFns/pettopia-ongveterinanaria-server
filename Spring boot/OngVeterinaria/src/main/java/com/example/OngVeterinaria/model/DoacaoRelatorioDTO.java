package com.example.OngVeterinaria.model;

import java.time.LocalDate;
import java.util.List;

public class DoacaoRelatorioDTO {
    private List<PedidoModel> pedidos;
    private FuncionarioModel funcionario;

    public byte[] getRelatorio() {
        return relatorio;
    }

    public void setRelatorio(byte[] relatorio) {
        this.relatorio = relatorio;
    }

    private byte[] relatorio;
    // Getters e Setters

    public List<PedidoModel> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<PedidoModel> pedidos) {
        this.pedidos = pedidos;
    }

    public FuncionarioModel getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(FuncionarioModel funcionario) {
        this.funcionario = funcionario;
    }
}
