package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FuncionarioServices {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ComprovanteRendaRepository comprovanteRendaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private DenunciaRepository denunciaRepository;

    public Optional<FuncionarioModel> findByIdFuncionario(Long idFuncionario) {
        return funcionarioRepository.findById(idFuncionario);
    }

    public Optional<PedidoModel> findByIdPedido(Long idPedido) {
        return pedidoRepository.findById(idPedido);
    }

    public PedidoModel validarCodigoComprovante(String codigo) {
        // Recuperar o serviço pelo código do comprovante
        PedidoModel servico = pedidoRepository.findByCodigoComprovante(codigo);

        if (servico != null && servico.getStatusPedido().equals(StatusGeral.ANDAMENTO)) {  // Certificar que está em andamento
            return servico;  // Retorna o serviço se estiver em andamento
        }

        return null;  // Caso o código seja inválido ou o status não seja "em andamento"
    }

    public boolean concluirPedido(Long id) {
        Optional<PedidoModel> pedidoOpt = pedidoRepository.findById(id);

        if (pedidoOpt.isPresent()) {
            PedidoModel pedido = pedidoOpt.get();

            // Verifica se o status do pedido está como "ANDAMENTO"
            if (pedido.getStatusPedido() == StatusGeral.ANDAMENTO) {
                // Atualiza o status do pedido para "CONCLUIDO" e coloca o animal para adoção
                AnimalModel animalModel = pedido.getAnimal();
                animalModel.setAdocao(true);
                animalRepository.save(animalModel);
                pedido.setValido(true);
                pedido.setStatusPedido(StatusGeral.CONCLUIDO);
                pedidoRepository.save(pedido);

                // Desvincula o animal do cliente
                AnimalModel animal = pedido.getAnimal();
                if (animal != null) {
                    animal.setCliente(null);  // Define idCliente como null
                    animalRepository.save(animal);
                }
                return true;
            }
        }
        return false;  // Retorna false se o pedido não for encontrado ou se o status não estiver como "ANDAMENTO"
    }

    public boolean concluirPedidoAdocao(Long idPedido) throws Exception {
        // Busca o pedido pelo ID
        Optional<PedidoModel> pedidoOpt = pedidoRepository.findById(idPedido);

        if (pedidoOpt.isPresent()) {
            PedidoModel pedido = pedidoOpt.get();

            // Verifica se o status do pedido está como "ANDAMENTO"
            if (pedido.getStatusPedido() == StatusGeral.ANDAMENTO) {
                // Atualiza o status do pedido para "CONCLUIDO"
                pedido.setStatusPedido(StatusGeral.CONCLUIDO);
                pedido.setValido(true);
                pedidoRepository.save(pedido);

                // Vincula o animal ao cliente
                AnimalModel animal = pedido.getAnimal();
                if (animal != null) {
                    ClienteModel cliente = pedido.getCliente();
                    if (cliente != null) {
                        // Atualiza o cliente do animal
                        animal.setAdocao(false);
                        animal.setCliente(cliente);
                        animalRepository.save(animal);
                        return true;
                    } else {
                        throw new Exception("Cliente não encontrado no pedido.");
                    }
                } else {
                    throw new Exception("Animal não encontrado no pedido.");
                }
            } else {
                throw new Exception("O pedido não está em andamento.");
            }
        } else {
            throw new Exception("Pedido não encontrado.");
        }
    }

    public List<FuncionarioModel> listarFuncionario(){
        return funcionarioRepository.findAll();
    }

    public List<PedidoModel> listarServicos(){
        return pedidoRepository.findAll();
    }


    public FuncionarioModel autenticar(String email, String passwordFuncionario) {
        FuncionarioModel response = funcionarioRepository.findByEmail(email);
        if (response == null) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // Verifica se a senha fornecida corresponde ao hash no banco de dados
        if (!passwordEncoder.matches(passwordFuncionario, response.getPasswordFuncionario())) {
            throw new RuntimeException("Credenciais inválidas");
        }
        return response;
    }

    //METODO PARA LISTAR TODAS AS DENÚNCIAS
    public List<DenunciaModel> listarTodasDenuncias() {
        return denunciaRepository.findAll();
    }

    // Buscar denuncia por ID
    public Optional<DenunciaModel> buscarDenunciaPorId(Long id) {
        return denunciaRepository.findById(id);
    }

    // Método que busca animais sem cliente vinculado
    public List<AnimalModel> listarAnimaisAdocao() {
        return animalRepository.findByClienteIsNull();  // Método customizado do repositório
    }
}