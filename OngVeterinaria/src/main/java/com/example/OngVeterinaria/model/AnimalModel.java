package com.example.OngVeterinaria.model;

import com.example.OngVeterinaria.model.Enum.Genero;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import com.example.OngVeterinaria.model.Enum.TipoEspecie;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "Tb_Animal")
public class AnimalModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_animal")
    private long idAnimal;

    @Column(name = "Nome")
    private String nome;

    @Column(name = "Especie")
    @Enumerated(EnumType.STRING)
    private TipoEspecie especie;

    @Column(name = "Raca")
    private String raca;

    @Column(name = "DataNascimento")
    private String idade;

    @Column(name = "Descricao")
    private String descricao;

    @Column(name = "Cor")
    private String cor;

    @Column(name = "sexo")
    @Enumerated(EnumType.STRING)
    private Genero sexo;

    @Column(name = "Peso")
    private double peso;

    @Lob
    @Column(name = "foto", columnDefinition = "LONGBLOB")
    private byte[] fotoAnimal;

    public Genero getSexo() {
        return sexo;
    }

    public void setSexo(Genero sexo) {
        this.sexo = sexo;
    }

    public boolean isAdocao() {
        return adocao;
    }

    public void setAdocao(boolean adocao) {
        this.adocao = adocao;
    }

    @Column(name = "Adocao")
    private boolean adocao;


    @ManyToOne
    @JoinColumn(name = "idCliente")
    private ClienteModel cliente;


    public long getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(long idAnimal) {
        this.idAnimal = idAnimal;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public byte[] getFotoAnimal() {
        return fotoAnimal;
    }

    public void setFotoAnimal( byte[] fotoAnimal) {
        this.fotoAnimal = fotoAnimal;
    }

    public String getIdade() {
        return idade;
    }

    public void setIdade(String idade) {
        this.idade = idade;
    }

    public TipoEspecie getEspecie() {
        return especie;
    }

    public void setEspecie(TipoEspecie especie) {
        this.especie = especie;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public AnimalModel(Long idAnimal) {
    }

    public AnimalModel() {
    }
}