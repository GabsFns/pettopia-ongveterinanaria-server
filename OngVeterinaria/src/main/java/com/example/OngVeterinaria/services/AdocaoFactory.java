package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.PedidoModel;
import com.example.OngVeterinaria.model.AnimalModel;
import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.Enum.StatusGeral;

public class AdocaoFactory {
    public PedidoModel criarAdocao(Long idCliente, Long idAnimal, byte[] comprovante) {
        PedidoModel adocao = new PedidoModel();
        ClienteModel cliente = new ClienteModel(idCliente);
        AnimalModel animal = new AnimalModel(idAnimal);

        adocao.setCliente(cliente);
        adocao.setAnimal(animal);
        adocao.setComprovante(comprovante);
        adocao.setStatusPedido(StatusGeral.PENDENTE);

        return adocao;
    }
}
