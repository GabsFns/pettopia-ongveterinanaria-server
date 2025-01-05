package com.example.OngVeterinaria.model;

import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;

@Entity
@Table(name = "tb_Denuncia")
public class DenunciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_denuncia")
    private Long idDenuncia; // Alterado para Long

    @Enumerated(EnumType.STRING)
    @Column(name = "Denuncias")
    private TipoDenucias tipoDenucias;

    @Column(name = "Descricao")
    private String descricao;

    @Column(name = "Data")
    private LocalDate dataDenuncia;

    public StatusGeral getStatusGeral() {
        return statusGeral;
    }

    public void setStatusGeral(StatusGeral statusGeral) {
        this.statusGeral = statusGeral;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private StatusGeral statusGeral;

    @ManyToOne
    @JoinColumn(name = "idCliente")

    private ClienteModel cliente;

    @ManyToOne
    @JoinColumn(name = "idEndereco")
    private EnderecoModel endereco;

    // Getters e Setters
    @PrePersist
    protected void onCreate() {
        this.dataDenuncia = LocalDate.now();  // Define a data de cadastro como a data atual
    }

    public Long getIdDenuncia() { // Alterado para Long
        return idDenuncia;
    }

    public void setIdDenuncia(Long idDenuncia) { // Alterado para Long
        this.idDenuncia = idDenuncia;
    }

    public TipoDenucias getTipoDenucias() {
        return tipoDenucias;
    }

    public void setTipoDenucias(TipoDenucias tipoDenucias) {
        this.tipoDenucias = tipoDenucias;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataDenuncia() {
        return dataDenuncia;
    }

    public void setDataDenuncia(LocalDate dataDenuncia) {
        this.dataDenuncia = dataDenuncia;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public EnderecoModel getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoModel endereco) {
        this.endereco = endereco;
    }
}

