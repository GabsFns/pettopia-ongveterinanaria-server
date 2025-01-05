package com.example.OngVeterinaria.controller;


import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoEspecie;
import com.example.OngVeterinaria.repository.ClienteRepository;
import com.example.OngVeterinaria.repository.DenunciaRepository;
import com.example.OngVeterinaria.repository.PedidoRepository;
import com.example.OngVeterinaria.services.AnimalServices;
import com.example.OngVeterinaria.services.ClienteServices;
import com.example.OngVeterinaria.services.FuncionarioServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/funcionario")
public class FuncionarioController {

    @Autowired
    private ClienteServices clienteServices;


    @Autowired
    private AnimalServices animalServices;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private FuncionarioServices funcionarioServices;

    @GetMapping("/ListarServicos")
    public ResponseEntity<List<PedidoModel>> listarServicos() {
        List<PedidoModel> servicos = funcionarioServices.listarServicos();
        return ResponseEntity.ok(servicos);
    }

    @GetMapping("/ListarAnimais")
    public ResponseEntity<List<AnimalModel>> listarAnimais() {
        List<AnimalModel> animais = animalServices.listarTodosAnimais();
        return ResponseEntity.ok(animais);
    }

    @GetMapping("/Listar/Denuncias")
    public ResponseEntity<List<DenunciaModel>> listarDenuncias() {
        List<DenunciaModel> denuncias = funcionarioServices.listarTodasDenuncias();
        return ResponseEntity.ok(denuncias);
    }

    // Endpoint para listar animais sem cliente vinculado
    @GetMapping("/ListarAnimaisAdocao")
    public ResponseEntity<List<AnimalModel>> listarAnimaisSemCliente() {
        List<AnimalModel> animaisSemCliente = funcionarioServices.listarAnimaisAdocao();

        // Verifica se a lista está vazia
        if (animaisSemCliente.isEmpty()) {
            return ResponseEntity.noContent().build();  // Retorna 204 se não houver animais
        }
        return ResponseEntity.ok(animaisSemCliente);  // Retorna 200 com os dados
    }

    //Pesquisar Usuario na tabela Desktop - FILTRO GET CLIENTE
    @GetMapping("/ClienteBuscar/{id}")
    public ResponseEntity<ClienteModel> buscarClientePorId(@PathVariable Long id) {
        Optional<ClienteModel> cliente = clienteServices.buscarClientePorId(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    //Pesquisar pedido de serviço na tabela Desktop - FILTRO GET SERVICO
    @GetMapping("/buscarPedido/{id}")
    public ResponseEntity<PedidoModel> buscarPedidoPorId(@PathVariable Long id) {
        Optional<PedidoModel> servico = funcionarioServices.findByIdPedido(id);
        return servico.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/EstatisticasEmAndamento")
    public ResponseEntity<Map<String, Long>> getEstatisticasEmAndamento() {
        // Contando doações e adoções em andamento
        Map<String, Long> doacoesEAdocoes = pedidoRepository.countDoacoesAndAdocoesEmAndamento();
        long denunciasEmAndamento = denunciaRepository.countDenunciasEmAndamento();

        // Log para depuração
        System.out.println("Doações em andamento: " + doacoesEAdocoes.get("doacoesEmAndamento"));
        System.out.println("Adoções em andamento: " + doacoesEAdocoes.get("adocoesEmAndamento"));
        System.out.println("Denúncias em andamento: " + denunciasEmAndamento);

        // Preenchendo o mapa final
        Map<String, Long> estatisticas = new HashMap<>();
        estatisticas.put("doacoesEmAndamento", doacoesEAdocoes.get("doacoesEmAndamento"));
        estatisticas.put("adocoesEmAndamento", doacoesEAdocoes.get("adocoesEmAndamento"));
        estatisticas.put("denunciasEmAndamento", denunciasEmAndamento);

        return ResponseEntity.ok(estatisticas);
    }

    @PostMapping("/LoginNormal")
    public ResponseEntity<?> loginNormal(@RequestBody FuncionarioModel loginRequest) {
        try {
            FuncionarioModel funcionarioModel = funcionarioServices.autenticar(loginRequest.getEmail(), loginRequest.getPasswordFuncionario());
            return ResponseEntity.ok(funcionarioModel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas: " + e.getMessage());
        }
    }

    @PutMapping("/validarCodigoComprovante/{codigo}")
    public ResponseEntity<PedidoModel> validarCodigoComprovante(@PathVariable String codigo) {
        PedidoModel servico = funcionarioServices.validarCodigoComprovante(codigo);

        if (servico == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);  // Caso o código não seja válido
        }

        return ResponseEntity.ok(servico);
    }

    @PutMapping("/concluirPedido/{id}")
    public ResponseEntity<?> concluirPedido(@PathVariable Long id) {
        boolean atualizado = funcionarioServices.concluirPedido(id);
        return atualizado ? ResponseEntity.ok("Pedido concluído com sucesso.")
                : ResponseEntity.status(404).body("Pedido encontrado ou em status inválido.");
    }

    @PutMapping("/concluirPedidoAdocao/{idPedido}")
    public ResponseEntity<?> concluirPedidoAdocao(@PathVariable Long idPedido) {
        try {
            boolean pedidoConcluido = funcionarioServices.concluirPedidoAdocao(idPedido);

            if (pedidoConcluido) {
                return ResponseEntity.ok("Pedido concluído e animal vinculado ao cliente.");
            } else {
                return ResponseEntity.status(400).body("O pedido não está em andamento ou não foi encontrado.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao concluir o pedido: " + e.getMessage());
        }
    }

    @PostMapping("/cadastrar/animal")
    public ResponseEntity<AnimalModel> cadastrarAnimal(@RequestBody AnimalModel animalRequest) {
        ClienteModel cliente = null;

        // Se o cliente foi enviado e tem um ID, verifica se ele existe
        if (animalRequest.getCliente() != null && animalRequest.getCliente().getIdCliente() != null) {
            Long idCliente = animalRequest.getCliente().getIdCliente();
            cliente = clienteRepository.findById(idCliente).orElse(null);  // Alterado para retornar null se o cliente não for encontrado
        }

        // Se não tiver cliente, considera o animal como disponível para adoção
        boolean adocao = animalRequest.isAdocao(); // Adoção padrão recebida do request
        if (cliente == null) {
            adocao = true;  // Se não houver cliente, o animal será vinculado a adoção
        }

        // Faz o cadastro do animal com ou sem cliente, e com o campo adoção ajustado
        AnimalModel animal = animalServices.cadastrarAnimal(
                cliente, // Se o cliente for null, o animal será cadastrado sem vínculo com cliente
                animalRequest.getNome(),
                animalRequest.getEspecie(),
                animalRequest.getSexo(),
                animalRequest.getRaca(),
                animalRequest.getIdade(),
                animalRequest.getCor(),
                animalRequest.getPeso(),
                animalRequest.getFotoAnimal(),
                animalRequest.getDescricao(),
                adocao // Adoção será true caso não haja cliente
        );

        return ResponseEntity.ok(animal);
    }


    // Atualizar um animal existente
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<AnimalModel> atualizarAnimal(
            @PathVariable Long id,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoEspecie especie,
            @RequestParam(required = false) String idade,
            @RequestParam(required = false) String cor,
            @RequestParam(required = false) double peso,
            @RequestParam(required = false) byte[] foto,
            @RequestParam(required = false) String descricao)
    {
        Optional<AnimalModel> atualizado = animalServices.atualizarAnimal(id, nome, especie, idade, cor, peso, foto, descricao);
        return atualizado.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Deletar um animal pelo ID
    @DeleteMapping("/deletarAnimal/{id}")
    public ResponseEntity<Void> deletarAnimal(@PathVariable Long id) {
        Optional<AnimalModel> deletado = animalServices.deletarAnimal(id);
        return deletado.isPresent() ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    //Pesquisar DENUNCIA na tabela Desktop - FILTRO GET DENUNCIA
    @GetMapping("/Buscar/Denuncia/{id}")
    public ResponseEntity<DenunciaModel> buscarDenunciaPorId(@PathVariable Long id) {
        Optional<DenunciaModel> denuncia = funcionarioServices.buscarDenunciaPorId(id);
        return denuncia.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
