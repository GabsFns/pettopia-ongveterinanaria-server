package com.example.OngVeterinaria.model;

import com.example.OngVeterinaria.model.Enum.TipoFuncionario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;

@Entity @Table(name = "Tb_Funcionario")
public class FuncionarioModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id_funcionario;

    @Column(name = "nome")
    @NotBlank(message = "Nome é obrigatório")
    private String nome_funcionario;

    @Column(name = "CPF")
    private String cpf_funcionario;

    @Column(name = "Email")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @Column(name = "Senha")
    private String passwordFuncionario;

    @Column(name = "DataEmissao")
    private LocalDate data_emissao;

    @Column(name = "Tipo")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Tipo de funcionário é obrigatório")
    private TipoFuncionario tipoFuncionario;

    public @NotBlank(message = "Nome é obrigatório") String getNome_funcionario() {
        return nome_funcionario;
    }

    public void setNome_funcionario(@NotBlank(message = "Nome é obrigatório") String nome_funcionario) {
        this.nome_funcionario = nome_funcionario;
    }

    public String getCpf_funcionario() {
        return cpf_funcionario;
    }

    public void setCpf_funcionario(String cpf_funcionario) {
        this.cpf_funcionario = cpf_funcionario;
    }

    public long getId_funcionario() {
        return id_funcionario;
    }

    public void setId_funcionario(long id_funcionario) {
        this.id_funcionario = id_funcionario;
    }

    public @NotBlank(message = "Email é obrigatório") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email é obrigatório") String email) {
        this.email = email;
    }

    public String getPasswordFuncionario() {
        return passwordFuncionario;
    }

    public void setPasswordFuncionario(String passwordFuncionario) {
        this.passwordFuncionario = passwordFuncionario;
    }

    public LocalDate getData_emissao() {
        return data_emissao;
    }

    public void setData_emissao(LocalDate data_emissao) {
        this.data_emissao = data_emissao;
    }

    public @NotNull(message = "Tipo de funcionário é obrigatório") TipoFuncionario getTipoFuncionario() {
        return tipoFuncionario;
    }

    public void setTipoFuncionario(@NotNull(message = "Tipo de funcionário é obrigatório") TipoFuncionario tipoFuncionario) {
        this.tipoFuncionario = tipoFuncionario;
    }

    @PrePersist
    protected void onCreate() {
        this.data_emissao = LocalDate.now();
    }

    // Getters e setters
}
