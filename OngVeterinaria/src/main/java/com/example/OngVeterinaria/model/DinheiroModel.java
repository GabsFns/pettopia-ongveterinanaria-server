package com.example.OngVeterinaria.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_doacoes")
public class DinheiroModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double valor;

    @Column(name = "data_doacao", nullable = false)
    private LocalDate dataDoacao;

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    @ManyToOne
    @JoinColumn(name = "idCliente")
    private ClienteModel cliente;

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }

    public LocalDate getDataDoacao() {
        return dataDoacao;
    }

    public void setDataDoacao(LocalDate dataDoacao) {
        this.dataDoacao = dataDoacao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "metodo_pagamento", nullable = false)
    private String metodoPagamento;

    public String getMes() {
        return dataDoacao.getMonth().toString(); // Retorna o nome do mês (ex: "JANUARY")
    }
}
