package com.example.OngVeterinaria.controller;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.example.OngVeterinaria.DTO.DenunciaDTO;
import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.*;
import com.example.OngVeterinaria.repository.*;
import com.example.OngVeterinaria.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("api/cliente")
public class ClienteController {

    @Autowired
    private ClienteServices clienteService;

    @Autowired
    private AnimalServices animalServices;

    @Autowired
    private PedidoServices pedidoServices;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private DinheiroRepository dinheiroRepository;

    @Autowired
    private ComprovanteRendaRepository comprovanteRendaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoRepository adocaoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private ComprovanteRendaServices comprovanteRendaServices;
    private final ObjectMapper objectMapper;


    @Autowired
    private JwtService jwtService;

    //Cadastrar Usuario
    @PostMapping("/cadastrar")
    public ResponseEntity<?> cadastrarCliente(@Valid @RequestBody ClienteModel clienteModel, BindingResult result) {
        // Tratamento de erro
        if (result.hasErrors()) {
            List<String> erros = result.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(erros);
        }
        // Criar DataCadastro Automática
        clienteModel.setData_Cadastro(LocalDate.now());
        try {
            // Salvando Novo cliente
            ClienteModel clienteSalvo = clienteService.cadastrarCliente(clienteModel);
            String token = jwtService.generateToken(clienteSalvo.getEmail());
            AuthResponse response = new AuthResponse(token, clienteSalvo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    @PostMapping("/cadastrarJava")
    public ResponseEntity<?> cadastrarClienteJava(@Valid @RequestBody ClienteModel clienteModel, BindingResult result) {
        // Tratamento de erro
        if (result.hasErrors()) {
            List<String> erros = result.getAllErrors().stream()
                    .map(objectError -> objectError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(erros);
        }

        // Criar DataCadastro Automática
        clienteModel.setData_Cadastro(LocalDate.now());

        // Definir senha como nula para cadastro presencial
        clienteModel.setPassword_Cliente(null);

        try {
            // Salvando Novo cliente
            ClienteModel clienteSalvo = clienteService.cadastrarClienteJava(clienteModel);
            return ResponseEntity.ok(clienteSalvo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    //Atualizar dados do usuario
    @PutMapping("/atualizar/{id}")
    public ResponseEntity<?> atualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteModel clienteAtualizado) {
        Optional<ClienteModel> clienteAtualizadoResposta = clienteService.atualizarCliente(id, clienteAtualizado);

        // Verifica se o cliente foi encontrado e atualizado
        if (clienteAtualizadoResposta.isPresent()) {
            return ResponseEntity.ok(clienteAtualizadoResposta.get());
        } else {
            return ResponseEntity.status(404).body("Cliente não encontrado.");
        }
    }

    @DeleteMapping("/deletarCliente/{idCliente}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long idCliente) {
        boolean deletado = clienteService.deletarCliente(idCliente);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/atualizarAnimal/{id}")
    public ResponseEntity<?> atualizarAnimal(@PathVariable Long id, @RequestBody AnimalModel animalDTO) {
        // Chama o método do service para atualizar o animal
        Optional<AnimalModel> animalAtualizado = animalServices.atualizarAnimal(
                id,
                animalDTO.getNome(),
                animalDTO.getEspecie(),
                animalDTO.getIdade(),
                animalDTO.getCor(),
                animalDTO.getPeso(),
                animalDTO.getFotoAnimal(),
                animalDTO.getDescricao()
        );

        if (animalAtualizado.isPresent()) {
            return ResponseEntity.ok(animalAtualizado.get());
        } else {
            return ResponseEntity.status(404).body("Animal não encontrado.");
        }
    }
    // Endpoint para pegar as opções de espécies e raças
    @GetMapping("/opcoes")
    public Map<String, Object> getOpcoes() {
        Map<String, Object> opcoes = new HashMap<>();

        // Adiciona as opções de espécies e suas raças
        Map<String, String[]> especiesComRacas = new HashMap<>();
        especiesComRacas.put(TipoEspecie.GATO.name(), Stream.of(TipoEspecie.RacaGato.values())
                .map(Enum::name).toArray(String[]::new));
        especiesComRacas.put(TipoEspecie.CACHORRO.name(), Stream.of(TipoEspecie.RacaCachorro.values())
                .map(Enum::name).toArray(String[]::new));

        // Adiciona as espécies e raças
        opcoes.put("especies", especiesComRacas);

        // Adiciona as opções de idade
        opcoes.put("idades", Stream.of(IdadeAnimal.values())
                .map(Enum::name).toArray(String[]::new));


        return opcoes;
    }

    public ClienteController(ObjectMapper objectMapper, ClienteRepository clienteRepository, AnimalServices animalServices) {
        this.objectMapper = objectMapper;
        this.clienteRepository = clienteRepository;
        this.animalServices = animalServices;
    }

    //SELECIONAR O ANIMAL PARA ENVIAR PARA ADOCAO
    @PostMapping(value = "/SelecaoAnimal/Adocao", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> SelecionarAnimalAdocao(@RequestPart("animal") String animalJson, // Mude para String
                                                         @RequestPart("fotoAnimal") MultipartFile fotoAnimal) throws IOException {

        // Converte o JSON para o objeto AnimalMode
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Re-registrando o módulo caso necessário
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        AnimalModel animalRequest = objectMapper.readValue(animalJson, AnimalModel.class);

        // Verifica se o cliente existe
        Long idCliente = animalRequest.getCliente().getIdCliente();
        ClienteModel cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Verifica se o animal existe e está relacionado ao cliente
        Optional<AnimalModel> optionalAnimal = animalRepository.findByIdAnimalAndCliente(animalRequest.getIdAnimal(), cliente);
        if (optionalAnimal.isPresent()) {
            AnimalModel animal = optionalAnimal.get();

            // Atualiza o status de adoção do animal
            animal.setAdocao(true);
            animalRepository.save(animal);

            // Gera o código de comprovação
            String codigoComprovacao = UUID.randomUUID().toString();

            try {
                // Gera o PDF
                System.out.println("Gerando PDF para: " + animal.getNome() + ", Cliente: " + cliente.getNome());
                byte[] pdfBytes = animalServices.gerarComprovantePDF(animal, cliente, codigoComprovacao);

                System.out.println("Tamanho do PDF gerado: " + pdfBytes.length);
                // Envia o email com o PDF
                animalServices.enviarComprovanteComPDF(cliente.getEmail(), pdfBytes, "comprovante_adocao.pdf");

                // Retorna o PDF
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(pdfBytes);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            // Retorna erro se o animal não existe ou não está relacionado ao cliente
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PostMapping(value = "/cadastrar/pedido/doacao", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PedidoModel> cadastrarPedido(@RequestBody PedidoModel pedidoModel) {
        try {
            // Extrai os modelos Animal e Cliente de ServicoModel
            AnimalModel animal = pedidoModel.getAnimal();
            ClienteModel cliente = pedidoModel.getCliente();

            // Verifica se o animal e o cliente existem no banco de dados
            animal = animalRepository.findById(animal.getIdAnimal())
                    .orElseThrow(() -> new RuntimeException("Animal não encontrado"));
            cliente = clienteRepository.findById(cliente.getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            // Verifica se já existe um pedido de doação pendente para este animal e cliente
            Optional<PedidoModel> pedidoExistente = adocaoRepository.findByAnimal_IdAnimalAndCliente_IdClienteAndStatusPedido(
                    animal.getIdAnimal(), cliente.getIdCliente(), StatusGeral.PENDENTE);

            if (pedidoExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(null); // Pedido de doação já existente
            }

            // Verificação de foto e descrição do animal
            if (animal.getFotoAnimal() == null || animal.getFotoAnimal().length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null); // Ou enviar uma mensagem de erro apropriada
            }

            if (animal.getDescricao() == null || animal.getDescricao().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null); // Ou enviar uma mensagem de erro apropriada
            }

            // Gera o código de comprovação
            String codigoComprovacao = UUID.randomUUID().toString();

            // Gera o PDF e insere o PDF no modelo de serviço
            byte[] pdfBytes = animalServices.gerarComprovantePDF(animal, cliente, codigoComprovacao);

            // Envia o e-mail com o PDF
            animalServices.enviarComprovanteComPDF(cliente.getEmail(), pdfBytes, "comprovante_doacao.pdf");

            // Preenche o ServicoModel com os dados necessários
            PedidoModel servicoResposta = new PedidoModel();
            servicoResposta.setAnimal(animal);
            servicoResposta.setCliente(cliente);
            servicoResposta.setCodigoComprovante(codigoComprovacao);
            servicoResposta.setComprovante(pdfBytes);  // PDF incluído no modelo de resposta

            // Retorna o ServicoModel como JSON
            return ResponseEntity.ok(servicoResposta);

        } catch (RuntimeException e) {
            // Retorna erro em caso de falha na busca
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value = "/cadastrar/animal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> cadastrarAnimal(
            @RequestPart("animal") String animalJson, // Mude para String
            @RequestPart("fotoAnimal") MultipartFile fotoAnimal) throws IOException {

        // Converte o JSON para o objeto AnimalModel
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Re-registrando o módulo caso necessário
        AnimalModel animalRequest = objectMapper.readValue(animalJson, AnimalModel.class);

        // Verifica se o cliente existe
        Long idCliente = animalRequest.getCliente().getIdCliente();
        ClienteModel cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Cadastra o animal
        AnimalModel animal = animalServices.cadastrarAnimal(
                cliente,
                animalRequest.getNome(),
                animalRequest.getEspecie(),
                animalRequest.getSexo(),
                animalRequest.getRaca(),
                animalRequest.getIdade(),
                animalRequest.getCor(),
                animalRequest.getPeso(),
                fotoAnimal.getBytes(), // Converte o MultipartFile em um array de bytes
                animalRequest.getDescricao(),
                animalRequest.isAdocao()
        );
        System.out.println("Cliente: " + cliente);
        System.out.println("Animal: " + animal);
        // Gera o código de comprovação
        String codigoComprovacao = UUID.randomUUID().toString();

        try {
            // Gera o PDF
            System.out.println("Gerando PDF para: " + animal.getNome() + ", Cliente: " + cliente.getNome());
            byte[] pdfBytes = animalServices.gerarComprovantePDF(animal, cliente, codigoComprovacao);

            System.out.println("Tamanho do PDF gerado: " + pdfBytes.length);
            // Envia o email com o PDF
            animalServices.enviarComprovanteComPDF(cliente.getEmail(), pdfBytes, "comprovante_adocao.pdf");

            // Retorna o PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//    @PostMapping(value = "/cadastrar/pedido/adocao", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<PedidoModel> cadastrarPedidoAdocao(@RequestBody PedidoAdocaoRequest pedidoAdocaoRequest) {
//        try {
//            PedidoModel pedidoModel = pedidoAdocaoRequest.getPedido();
//            byte[] comprovanteRenda = pedidoAdocaoRequest.getComprovanteRenda();
//            AnimalModel animal = pedidoModel.getAnimal();
//            ClienteModel cliente = pedidoModel.getCliente();
//
//            // Verifica se o animal tem um cliente vinculado e se já existe um pedido de adoção
//            if (animal.getCliente() != null || adocaoRepository.findByAnimal_IdAnimalAndCliente_IdClienteAndTipo(
//                    animal.getIdAnimal(), cliente.getIdCliente(), PedidosTipo.ADOCAO).isPresent()) {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
//            }
//
//            // Gera código de comprovação e PDF
//            String codigoComprovacao = UUID.randomUUID().toString();
//            byte[] pdfBytes = animalServices.gerarComprovantePDFAdocao(animal, cliente, codigoComprovacao);
//            animalServices.enviarComprovanteComPDF(cliente.getEmail(), pdfBytes, "comprovante_adocao.pdf");
//
//            // Salva o pedido de adoção
//            pedidoModel.setCodigoComprovante(codigoComprovacao);
//            pedidoModel.setTipo(PedidosTipo.ADOCAO);
//            PedidoModel pedidoSalvo = adocaoRepository.save(pedidoModel);
//
//            // Salva o comprovante de renda em ComprovanteRendaModel e associa ao pedido
//            ComprovanteRendaModel comprovanteModel = new ComprovanteRendaModel();
//            comprovanteModel.setComprovante(comprovanteRenda);
//            comprovanteModel.setPedido(pedidoSalvo);
//            comprovanteRendaRepository.save(comprovanteModel);
//
//            return ResponseEntity.ok(pedidoSalvo);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

    //Cadastrar animal Normalmente sem manipulacao apenas criar
    @PostMapping(value = "/cadastrar/animalNormal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> cadastrarAnimalNormal(
            @RequestPart("animal") String animalJson, // Recebe o JSON como String
            @RequestPart("fotoAnimal") MultipartFile fotoAnimal) throws IOException {

        // Converte o JSON para o objeto AnimalModel
        AnimalModel animalRequest = objectMapper.readValue(animalJson, AnimalModel.class);

        // Verifica se o cliente existe
        Long idCliente = animalRequest.getCliente().getIdCliente();
        ClienteModel cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        // Verifica se a foto do animal foi recebida
        if (fotoAnimal.isEmpty()) {
            throw new RuntimeException("A foto do animal não pode ser vazia");
        }

        // Cadastra o animal
        AnimalModel animal = animalServices.cadastrarAnimal(
                cliente,
                animalRequest.getNome(),
                animalRequest.getEspecie(),
                animalRequest.getSexo(),
                animalRequest.getRaca(),
                animalRequest.getIdade(),
                animalRequest.getCor(),
                animalRequest.getPeso(),
                fotoAnimal.getBytes(), // Converte o MultipartFile em um array de bytes
                animalRequest.getDescricao(),
                animalRequest.isAdocao()
        );

        // Resposta de sucesso, pode incluir detalhes do animal cadastrado
        return ResponseEntity.ok().body("Animal cadastrado com sucesso!".getBytes());
    }

    //Filtro para pesquisar animal
    @GetMapping("/buscaAnimal")
    public ResponseEntity<List<AnimalModel>> buscarAnimais(
            @RequestParam(required = false) TipoEspecie especie,
            @RequestParam(required = false) String raca) {

        List<AnimalModel> animais = animalServices.buscarAnimaisPorFiltros(especie, raca);
        return ResponseEntity.ok(animais);
    }

    //Sistema Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody ClienteModel loginRequest) {
        try {
            //se os dados baterem vai deixar o usuario logar, instaciando o metodo login
            Optional<ClienteModel> clienteModel = clienteService.login(loginRequest.getEmail(), loginRequest.getPassword_Cliente());
            return ResponseEntity.ok(clienteModel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Credenciais inválidas: " + e.getMessage());
        }
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> gerarTokenRecuperacao(@RequestBody ClienteModel emailRequest) {
        try {
            // Obter o email da classe DTO
            String email = emailRequest.getEmail();
            // Instanciar o método gerarToken passando o email do cliente
            clienteService.gerarTokenRecuperacao(email);
            return ResponseEntity.ok().body("Token enviado para o e-mail.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Erro ao enviar o token: " + e.getMessage());
        }
    }

    @GetMapping("/ExibirAdocoesAnimais")
    public ResponseEntity<List<AnimalModel>> getAnimaisParaAdocao() {
        List<AnimalModel> animaisParaAdocao = animalServices.getAnimaisParaAdocao();
        return new ResponseEntity<>(animaisParaAdocao, HttpStatus.OK);
    }

    @GetMapping("/doacao-validas")
    public ResponseEntity<List<PedidoModel>> getAdocoesDoacaoValidas() {
        List<PedidoModel> adocoes = pedidoServices.listarAdocoesDoacaoValidas();
        return new ResponseEntity<>(adocoes, HttpStatus.OK);
    }

    @GetMapping("/doacao/{idPedido}/download")
    public ResponseEntity<byte[]> downloadComprovantee(@PathVariable Long idPedido) {
        Optional<PedidoModel> adocaoOptional = adocaoRepository.findById(idPedido);
        if (adocaoOptional.isPresent()) {
            PedidoModel adocao = adocaoOptional.get();
            byte[] comprovante = adocao.getComprovante();

            // Define o tipo de conteúdo para PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", adocao.getCodigoComprovante() + ".pdf");

            return new ResponseEntity<>(comprovante, headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/doacao/{idPedido}/view")
    public ResponseEntity<byte[]> viewComprovante(@PathVariable Long idPedido) {
        Optional<PedidoModel> adocaoOptional = adocaoRepository.findById(idPedido);
        if (adocaoOptional.isPresent()) {
            PedidoModel adocao = adocaoOptional.get();
            byte[] comprovante = adocao.getComprovante();

            // Define o tipo de conteúdo para PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            return new ResponseEntity<>(comprovante, headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/adocao/{id}/download")
    public ResponseEntity<byte[]> downloadComprovanteAdocao(@PathVariable Long id) {
        Optional<PedidoModel> adocaoOptional = adocaoRepository.findById(id);
        if (adocaoOptional.isPresent()) {
            PedidoModel adocao = adocaoOptional.get();
            byte[] comprovante = adocao.getComprovante();

            // Define o tipo de conteúdo para PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", adocao.getCodigoComprovante() + ".pdf");

            return new ResponseEntity<>(comprovante, headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/adocao/{id}/view")
    public ResponseEntity<byte[]> viewComprovanteAdocao(@PathVariable Long id) {
        Optional<PedidoModel> adocaoOptional = adocaoRepository.findById(id);
        if (adocaoOptional.isPresent()) {
            PedidoModel adocao = adocaoOptional.get();
            byte[] comprovante = adocao.getComprovante();

            // Define o tipo de conteúdo para PDF
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);

            return new ResponseEntity<>(comprovante, headers, HttpStatus.OK);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/doacoes/{idCliente}")
    public ResponseEntity<List<PedidoModel>> buscarComprovantesPorCliente(@PathVariable Long idCliente) {
        List<PedidoModel> adocoes = adocaoRepository.findByCliente_IdCliente(idCliente);
        return ResponseEntity.ok(adocoes);
    }

    //Validcao do token
    @PostMapping("/validar-token")
    public ResponseEntity<?> validarToken(@RequestBody validarToken request) {
        String email = request.getEmail();
        String token = request.getToken();

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("O email não pode ser nulo ou vazio");
        }

        boolean valido = clienteService.validarToken(email, token);
        if (valido) {
            return ResponseEntity.ok().body("Token válido.");
        } else {
            return ResponseEntity.status(400).body("Token inválido ou expirado.");
        }
    }

    //Atualizacao de senha
    @PatchMapping("/atualizar-senha")
    public ResponseEntity<?> atualizarSenha(@RequestBody ClienteModel request) {
        try {
            String email = request.getEmail();
            String novaSenha = request.getPassword_Cliente();
            clienteService.atualizarSenha(email, novaSenha);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Senha Atualizada com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar a senha.");
        }
    }

    @PostMapping("/RealizarDenuncia")
    public ResponseEntity<?> realizarDenuncia(@RequestBody DenunciaModel denunciaModel) {
        try {
            // Verifica se o cliente está presente
            if (denunciaModel.getCliente() == null || denunciaModel.getCliente().getIdCliente() == null) {
                return ResponseEntity.badRequest().body("Cliente é obrigatório para realizar a denúncia.");
            }

            // Verifica se o endereço está presente
            if (denunciaModel.getEndereco() == null) {
                return ResponseEntity.badRequest().body("Endereço é obrigatório para realizar a denúncia.");
            }

            // Define o status inicial da denúncia
            denunciaModel.setDataDenuncia(LocalDate.now());
            denunciaModel.setStatusGeral(StatusGeral.PENDENTE);

            // Realiza a denúncia
            DenunciaModel novaDenuncia = clienteService.realizarDenuncia(denunciaModel);

            // Retorna a nova denúncia criada com sucesso
            return ResponseEntity.ok(novaDenuncia);
        } catch (Exception e) {
            e.printStackTrace(); // Adicione esta linha
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao realizar denúncia: " + e.getMessage());
        }
    }

    @DeleteMapping("/ExcluirDenuncia/{idDenuncia}")
    public ResponseEntity<String> excluirDenuncia(@PathVariable Long idDenuncia) {
        System.out.println("ID da denúncia recebida: " + idDenuncia); // Verifica se o ID chega corretamente

        Optional<DenunciaModel> denunciaOpt = denunciaRepository.findById(idDenuncia);
        if (denunciaOpt.isPresent()) {
            denunciaRepository.deleteById(idDenuncia);
            return ResponseEntity.ok("Denúncia excluída com sucesso!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Denúncia não encontrada.");
        }
    }

    @GetMapping("/totalDenuncias/{idCliente}")
    public ResponseEntity<Long> totalDenuncias(@PathVariable Long idCliente) {
        long total = denunciaRepository.countByClienteIdCliente(idCliente); // Método que conta as denúncias pelo ID do cliente
        return ResponseEntity.ok(total);
    }

    @GetMapping("/graficoDoacao/{idCliente}")
    public ResponseEntity<List<DinheiroModel>> getDoacoesByCliente(@PathVariable Long idCliente) {
        List<DinheiroModel> doacoes = dinheiroRepository.findByClienteIdCliente(idCliente);
        return ResponseEntity.ok(doacoes);
    }

    @GetMapping("/denunciasTipoGrafico/{idCliente}")
    public ResponseEntity<List<Map<String, Object>>> getDenunciaCountByTipo(@PathVariable Long idCliente) {
        List<Object[]> resultados = denunciaRepository.countDenunciasByTipo(idCliente);

        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] resultado : resultados) {
            Map<String, Object> item = new HashMap<>();
            item.put("tipoDenuncia", resultado[0]);  // Tipo de denúncia
            item.put("count", resultado[1]);  // Contagem de denúncias desse tipo
            response.add(item);
        }

        return ResponseEntity.ok(response);
    }

    @PutMapping("/AtualizarDenuncia/{idDenuncia}")
    public ResponseEntity<DenunciaModel> atualizarDenuncia(@PathVariable Long idDenuncia, @RequestBody DenunciaModel denunciaAtualizada) {
        try {
            DenunciaModel denuncia = clienteService.atualizarDenuncia(idDenuncia, denunciaAtualizada);
            return ResponseEntity.ok(denuncia);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/BuscarDenuncias/{idCliente}")
    public List<DenunciaDTO> listarPorCliente(@PathVariable Long idCliente) {
        return clienteService.buscarPorCliente(idCliente);
    }

    @GetMapping("/data/{dataDenuncia}")
    public List<DenunciaModel> listarPorData(@PathVariable LocalDate dataDenuncia) {
        return clienteService.buscarPorData(dataDenuncia);
    }

    @GetMapping("/tipo/{tipoDenucias}")
    public List<DenunciaModel> listarPorTipo(@PathVariable TipoDenucias tipoDenucias) {
        return clienteService.buscarPorTipo(tipoDenucias);
    }

    @DeleteMapping("/deletarAnimal/{animalId}")
    public ResponseEntity<String> deletarAnimal(@PathVariable Long animalId) {
        // Verifica se o animal está na tabela tbAdocao
        boolean existeNaAdocao = animalRepository.existsByIdAnimalAndAdocaoTrue(animalId);

        if (existeNaAdocao) {
            // Retorna um erro 400 com mensagem personalizada
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Animal não pode ser deletado porque está associado a uma adoção.");
        }

        // Caso não esteja, deleta o animal
        animalRepository.deleteById(animalId);
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }

    @GetMapping("/filtro")
    public List<DenunciaModel> listarPorClienteDataETipo(
            @RequestParam Long idCliente,
            @RequestParam LocalDate dataDenuncia,
            @RequestParam TipoDenucias tipoDenucias) {
        return clienteService.buscarPorClienteDataETipo(idCliente, dataDenuncia, tipoDenucias);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<String> atualizarStatus(@PathVariable Long id, @RequestParam StatusGeral novoStatus) {
        pedidoServices.atualizarStatus(id, novoStatus);
        return ResponseEntity.ok("Status da adoção atualizado com sucesso!");
    }

    @GetMapping("/comprovante/gerar/{id}")
    public ResponseEntity<byte[]> gerarEEnviarComprovante(@PathVariable Long id) throws MessagingException {
        PedidoModel adocao = adocaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoção não encontrada"));

        byte[] comprovanteAprovacao = pedidoServices.gerarComprovanteAprovacao(adocao);
        pedidoServices.enviarEmailComprovante(adocao.getCliente().getEmail(), comprovanteAprovacao);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprovante_adoção.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(comprovanteAprovacao);
    }

    @GetMapping("/comprovante/{id}")
    public ResponseEntity<byte[]> downloadComprovante(@PathVariable Long id) {
        PedidoModel adocao = adocaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoção não encontrada"));
        byte[] comprovante = adocao.getComprovante();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprovante.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(comprovante);
    }

    @GetMapping("/ListarAnimais/{idCliente}")
    public ResponseEntity<List<AnimalModel>> getAnimaisByClienteId(@PathVariable Long idCliente) {
        List<AnimalModel> animais = animalServices.findAnimalsByIdCliente(idCliente);
        return ResponseEntity.ok(animais);
    }

    @GetMapping("/ListarAnimais/Modal/{idAnimal}")
    public ResponseEntity<AnimalModel> getAnimalById(@PathVariable("idAnimal") Long idAnimal) {
        Optional<AnimalModel> animal = animalServices.findByIdAnimal(idAnimal);
        if (animal.isPresent()) {
            return ResponseEntity.ok(animal.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/validar/{idDocumento}")
    public ResponseEntity<?> validarDocumento(
            @PathVariable Long idDocumento,
            @RequestBody Map<String, String> request) {

        try {
            StatusGeral novoStatus = StatusGeral.valueOf(request.get("status"));
            PedidoModel adocaoCriada = pedidoServices.validarAdocao(idDocumento, novoStatus);
            return ResponseEntity.ok(adocaoCriada);  // Retorna a adoção criada
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao validar documento: " + e.getMessage());
        }
    }

    @GetMapping("/ListarDocumentosCliente/{idCliente}")
    public ResponseEntity<List<ComprovanteRendaModel>> listarDocumentosPendentesCliente(@PathVariable("idCliente") Long idCliente) {
        List<ComprovanteRendaModel> documentos = comprovanteRendaServices.buscarDocumentosCliente(idCliente);
        return ResponseEntity.ok(documentos);
    }

    // Endpoint para listar documentos pendentes
    @GetMapping("/ListarDocumentos")
    public ResponseEntity<List<ComprovanteRendaModel>> listarDocumentosPendentes() {
        List<ComprovanteRendaModel> documentos = pedidoServices.buscarDocumentos();
        return ResponseEntity.ok(documentos);
    }

    @PostMapping(value = "/enviarDocumentos", consumes = "multipart/form-data")
    public ResponseEntity<?> enviarDocumentos(
            @RequestPart("idCliente") Long idCliente,
            @RequestPart("idAnimal") Long idAnimal,
            @RequestPart("comprovante") MultipartFile comprovante) {

        System.out.println("ID Cliente: " + idCliente);
        System.out.println("ID Animal: " + idAnimal);
        System.out.println("Comprovante: " + comprovante.getOriginalFilename());

        try {

            // Chama o serviço para salvar os documentos e retorna o documento criado
            ComprovanteRendaModel documentoSalvo = pedidoServices.salvarDocumentos(idCliente, idAnimal, comprovante.getBytes());
            Map<String, Object> response = new HashMap<>();
            // Retorna o documento salvo como resposta
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar os documentos: " + e.getMessage());
        }
    }

    @PostMapping(value = "/enviarDocumentosTeste", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ComprovanteRendaModel> enviarDocumentosTeste(
            @RequestParam("idCliente") Long idCliente,
            @RequestParam("idAnimal") Long idAnimal,
            @RequestParam("file") MultipartFile file,
            @RequestHeader Map<String, String> headers) {

        System.out.println("Headers: " + headers); // Log dos cabeçalhos

        // Verifica se o arquivo é vazio
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // Verifica se já existe um pedido de análise pendente associado ao mesmo animal e cliente
            Optional<ComprovanteRendaModel> comprovanteExistente = comprovanteRendaRepository
                    .findByCliente_IdClienteAndAnimal_IdAnimalAndStatus(idCliente, idAnimal, StatusGeral.PENDENTE);
            if (comprovanteExistente.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null); // Já existe um pedido de análise pendente para este cliente e animal
            }

            // Verifica se o animal já foi adotado (se possui cliente vinculado)
            AnimalModel animal = animalRepository.findById(idAnimal).orElseThrow(() -> new Exception("Animal não encontrado"));
            if (animal.getCliente() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null); // O animal já foi adotado
            }

            // Converte MultipartFile para byte[]
            byte[] fileBytes = file.getBytes();

            // Chama o serviço para salvar os documentos
            ComprovanteRendaModel documentoSalvo = pedidoServices.salvarDocumentos(idCliente, idAnimal, fileBytes);

            return ResponseEntity.ok(documentoSalvo);
        } catch (Exception e) {
            e.printStackTrace(); // Isso ajudará a visualizar o erro
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Retorna null em caso de erro
        }
    }
}


