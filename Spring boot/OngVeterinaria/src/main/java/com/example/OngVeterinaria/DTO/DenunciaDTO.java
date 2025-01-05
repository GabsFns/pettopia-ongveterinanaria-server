package com.example.OngVeterinaria.DTO;

import com.example.OngVeterinaria.model.EnderecoModel;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;

import java.time.LocalDate;
import java.util.List;

public class DenunciaDTO {
    private Long idDenuncia;
    private TipoDenucias tipoDenuncia;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusGeral getStatusGeral() {
        return statusGeral;
    }

    public void setStatusGeral(StatusGeral statusGeral) {
        this.statusGeral = statusGeral;
    }

    public LocalDate getDataDenuncia() {
        return dataDenuncia;
    }

    public void setDataDenuncia(LocalDate dataDenuncia) {
        this.dataDenuncia = dataDenuncia;
    }

    public TipoDenucias getTipoDenuncia() {
        return tipoDenuncia;
    }

    public void setTipoDenuncia(TipoDenucias tipoDenuncia) {
        this.tipoDenuncia = tipoDenuncia;
    }

    public Long getIdDenuncia() {
        return idDenuncia;
    }

    public void setIdDenuncia(Long idDenuncia) {
        this.idDenuncia = idDenuncia;
    }

    private String descricao;
    private LocalDate dataDenuncia;
    private StatusGeral statusGeral;

    public EnderecoDTO getEnderecoDenuncia() {
        return enderecoDenuncia;
    }

    public void setEnderecoDenuncia(EnderecoDTO enderecoDenuncia) {
        this.enderecoDenuncia = enderecoDenuncia;
    }

    private EnderecoDTO enderecoDenuncia;
}
