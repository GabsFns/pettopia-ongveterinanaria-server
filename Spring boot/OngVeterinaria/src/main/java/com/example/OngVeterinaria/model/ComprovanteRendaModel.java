package com.example.OngVeterinaria.model;

import com.example.OngVeterinaria.model.Enum.StatusGeral;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_comprovantes_renda")
public class ComprovanteRendaModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idDocumento;

    @Lob
    @Column(name = "Arquivo", columnDefinition = "LONGBLOB")
    private byte[] arquivo;

    @ManyToOne
    @JoinColumn(name = "idCliente", nullable = false)
    private ClienteModel cliente;

    @ManyToOne
    @JoinColumn(name = "idAnimal", nullable = false)
    private AnimalModel animal;

    @Column(name = "Status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusGeral status;  // Status inicial será "PENDENTE"

    @Column(name = "TempoExclusao")
    private LocalDateTime tempoExclusao;

    public LocalDateTime getTempoExclusao() {
        return tempoExclusao;
    }

    public void setTempoExclusao(LocalDateTime tempoExclusao) {
        this.tempoExclusao = tempoExclusao;
    }

    public AnimalModel getAnimal() {
        return animal;
    }

    public void setAnimal(AnimalModel animal) {
        this.animal = animal;
    }

    public StatusGeral getStatus() {
        return status;
    }

    public void setStatus(StatusGeral status) {
        this.status = status;
    }

    public ClienteModel getCliente() {
        return cliente;
    }

    public void setCliente(ClienteModel cliente) {
        this.cliente = cliente;
    }

    public byte[] getArquivo() {
        return arquivo;
    }

    public void setArquivo(byte[] arquivo) {
        this.arquivo = arquivo;
    }

    public long getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(long idDocumento) {
        this.idDocumento = idDocumento;
    }
}
