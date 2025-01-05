package com.example.OngVeterinaria.DTO;

import com.example.OngVeterinaria.model.Enum.Genero;

import java.time.LocalDate;
import java.util.List;

public class ClienteDTO {
    private Long idCliente;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Genero getGenero() {
        return genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    private String cpf;
    private String nome;
    private String email;
    private String telefone;
    private Genero genero;
    private LocalDate dataNascimento;

    public List<DenunciaDTO> getDenuncias() {
        return denuncias;
    }

    public void setDenuncias(List<DenunciaDTO> denuncias) {
        this.denuncias = denuncias;
    }

    private List<DenunciaDTO> denuncias;
}
