package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.DinheiroModel;
import com.example.OngVeterinaria.repository.ClienteRepository;
import com.example.OngVeterinaria.repository.DinheiroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DinheiroServices {
    @Autowired
    private DinheiroRepository dinheiroRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    public DinheiroModel salvarDoacaoComCliente(Double valor, Long clienteId) {
        DinheiroModel doacao = new DinheiroModel();
        ClienteModel cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));

        doacao.setValor(valor);
        doacao.setMetodoPagamento("Mercado Pago");
        doacao.setDataDoacao(LocalDate.now());
        doacao.setCliente(cliente);

        return dinheiroRepository.save(doacao);
    }

    public DinheiroModel salvarDoacao(Double valor) {
        DinheiroModel doacao = new DinheiroModel();


        doacao.setValor(valor);
        doacao.setMetodoPagamento("Mercado Pago");
        doacao.setDataDoacao(LocalDate.now());


        return dinheiroRepository.save(doacao);
    }
}
