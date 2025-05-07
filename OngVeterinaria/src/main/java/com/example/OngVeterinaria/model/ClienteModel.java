package com.example.OngVeterinaria.model;

import com.example.OngVeterinaria.model.Enum.Genero;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity //Transformando em uma entidade
@Table(name = "Tb_Cliente") //Criando Tabela no Banco de Dados
public class ClienteModel implements Serializable {

    @Id  //Identificar que IdCliente é uma Primary Key no banco de dados
    @GeneratedValue(strategy = GenerationType.IDENTITY) //AutoIncrement
    private Long idCliente;

    @Column(name = "Cpf")
    private String cpf;

    @Column(name = "Nome")
    private String nome;

    @Column(name = "Email")
    private String email;

    @Column(name = "Senha")
    private String password_Cliente;

    @Column(name = "Telefone")
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "Genero")
    private Genero generoCliente;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "DataNascimento")
    private LocalDate data_nascimento;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "DataCadastro")
    private LocalDate data_Cadastro;

    @Column(name = "ResetToken")
    private String resetToken;

    @Column(name = "ResetTokenExpiration")
    private LocalDateTime resetTokenExpiration;

    public ClienteModel(Long idCliente) {

    }
    public ClienteModel() {
    }

    @PrePersist
    protected void onCreate() {
        this.data_Cadastro = LocalDate.now();  // Define a data de cadastro como a data atual
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public LocalDate getData_Cadastro() {
        return data_Cadastro;
    }

    public void setData_Cadastro( LocalDate data_Cadastro) {
        this.data_Cadastro = data_Cadastro;
    }

    public  LocalDate getData_nascimento() {
        return data_nascimento;
    }

    public void setData_nascimento( LocalDate data_nascimento) {
        this.data_nascimento = data_nascimento;
    }

    public Genero getGeneroCliente() {
        return generoCliente;
    }

    public void setGeneroCliente(Genero generoCliente) {
        this.generoCliente = generoCliente;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone( String telefone) {
        this.telefone= telefone;
    }

    public String getPassword_Cliente() {
        return password_Cliente;
    }

    public void setPassword_Cliente( String password_Cliente) {
        this.password_Cliente = password_Cliente;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail( String email) {
        this.email = email;
    }

    public  String getNome() {
        return nome;
    }

    public void setNome( String nome) {
        this.nome = nome;
    }

    public  String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }


    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiration() {
        return resetTokenExpiration;
    }

    public void setResetTokenExpiration(LocalDateTime resetTokenExpiration) {
        this.resetTokenExpiration = resetTokenExpiration;
    }
}