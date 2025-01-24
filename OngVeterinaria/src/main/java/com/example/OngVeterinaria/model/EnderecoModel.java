package com.example.OngVeterinaria.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tb_Endereco") // Nome da tabela no banco de dados
public class EnderecoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEndereco;

    @NotBlank(message = "Campo Obrigatório")
    @Column(name = "Rua")
    private String logradouro;

    @Column(name = "Numero")
    private String numero;

    @NotBlank(message = "Campo Obrigatório")
    @Column(name = "Bairro")
    private String bairro;

    @Column(name = "Complemento")
    private String complemento;

    @NotBlank(message = "Campo Obrigatório")
    @Column(name = "Estado")
    private String uf;

    @NotBlank(message = "Campo Obrigatório")
    @Column(name = "Cep")
    private String cep;

    @Column(name = "localidade")
    @JsonProperty("localidade")
    private String localidade;

    // Getters e Setters
    public String getLocalidade() {
        return localidade;
    }

    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }

    public Long getIdEndereco() {
        return idEndereco;
    }

    public void setIdEndereco(Long idEndereco) {
        this.idEndereco = idEndereco;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }
}
