package com.example.OngVeterinaria.model;

import com.example.OngVeterinaria.model.Enum.PedidosTipo;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "Tb_Pedidos")
public class PedidoModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idPedido;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private StatusGeral StatusPedido;

    @Lob
    @Column(name = "Comprovante", columnDefinition = "LONGBLOB")
    private byte[] comprovante;

    @ManyToOne
    @JoinColumn(name = "IdCliente")
    private ClienteModel cliente;

    @ManyToOne
    @JoinColumn(name = "IdAnimal")
    private AnimalModel animal;

    @Column(name = "CodigoComprovante", unique = true)
    private String codigoComprovante;

    @Column(name = "Tipo")
    @Enumerated(EnumType.STRING)
    private PedidosTipo tipo;

    @Column(name = "Data")
    private LocalDate dataPedido;

    @Transient // Indica que este campo não será mapeado no banco de dados
    private String mes;

    public String getMes() {
        return dataPedido.getMonth().toString(); // Retorna o nome do mês (ex: "JANUARY")
    }

    @PrePersist
    protected void onCreate() {
        this.dataPedido = LocalDate.now();  // Define a data de cadastro como a data atual
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public Boolean getValido() {
        return valido;
    }

    public void setValido(Boolean valido) {
        this.valido = valido;
    }

    @Column(name = "Validado")
    private Boolean valido;



    public PedidosTipo getTipo() {
        return tipo;
    }

    public void setTipo(PedidosTipo tipo) {
        this.tipo = tipo;
    }



    public String getCodigoComprovante() {
        return codigoComprovante;
    }

    public void setCodigoComprovante(String codigoComprovante) {
        this.codigoComprovante = codigoComprovante;
    }



    public void setStatusPedido(StatusGeral statusAdocao) {
        StatusPedido = statusAdocao;
    }

    public byte[] getComprovante() {
        return comprovante;
    }

    public void setComprovante(byte[] comprovante) {
        this.comprovante = comprovante;
    }

    public AnimalModel getAnimal() {
        return animal;
    }

    public void setAnimal(AnimalModel animal) {
        this.animal = animal;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public StatusGeral getStatusPedido() {
        return StatusPedido;
    }


    public long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(long idAdocao) {
        this.idPedido = idAdocao;
    }
}