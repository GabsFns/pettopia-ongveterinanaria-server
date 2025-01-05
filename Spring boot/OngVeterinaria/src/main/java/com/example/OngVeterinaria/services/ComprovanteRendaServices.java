package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.ComprovanteRendaModel;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.repository.AnimalRepository;
import com.example.OngVeterinaria.repository.ClienteRepository;
import com.example.OngVeterinaria.repository.ComprovanteRendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComprovanteRendaServices {

    @Autowired
    private ComprovanteRendaRepository comprovanteRendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AnimalRepository animalRepository;

    public void salvarComprovante(ComprovanteRendaModel comprovanteRendaModel) {
        // Aqui você pode adicionar qualquer lógica de validação ou outras verificações
        comprovanteRendaRepository.save(comprovanteRendaModel);
    }

    // Buscar cliente por ID
    public Optional<ComprovanteRendaModel> buscarComprovantePorId(Long id) {
        return comprovanteRendaRepository.findById(id);
    }

    public List<ComprovanteRendaModel> buscarDocumentos() {
        return comprovanteRendaRepository.findAll();
    }

    public List<ComprovanteRendaModel> buscarDocumentosCliente(Long idCliente){
        return comprovanteRendaRepository.findByCliente_IdCliente(idCliente);
    }

    public ComprovanteRendaModel salvarDocumentos(Long idCliente, Long idAnimal, byte[] file) throws Exception {
        // Verifica se o arquivo não é nulo ou vazio
        if (file == null || file.length == 0) {
            throw new Exception("Arquivo não pode ser vazio");
        }

        // Lógica para salvar o documento
        ComprovanteRendaModel documento = new ComprovanteRendaModel();

        // Preenche os campos necessários
        documento.setCliente(clienteRepository.findById(idCliente)
                .orElseThrow(() -> new Exception("Cliente não encontrado")));
        documento.setAnimal(animalRepository.findById(idAnimal)
                .orElseThrow(() -> new Exception("Animal não encontrado")));

        // Salva o arquivo como byte array
        documento.setArquivo(file);

        // Define o status inicial
        documento.setStatus(StatusGeral.PENDENTE);

        // Salva o documento no repositório
        return comprovanteRendaRepository.save(documento);
    }
}