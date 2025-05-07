package com.example.OngVeterinaria.controller;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.example.OngVeterinaria.DTO.DenunciaDTO;
import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.*;
import com.example.OngVeterinaria.repository.*;
import com.example.OngVeterinaria.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Cadastrar um cliente",
            description = "Cadastra um cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao cadastrar cliente")
    })
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

    @Operation(
            summary = "Cadastrar um cliente presencialmente (desktop)",
            description = "Cadastra um cliente presencialmente através do sistema desktop sem uma senha"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente cadastrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao cadastrar cliente")
    })
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

        // Garante que o id seja nulo
        clienteModel.setIdCliente(null);

        try {
            // Salvando Novo cliente
            ClienteModel clienteSalvo = clienteService.cadastrarClienteJava(clienteModel);
            return ResponseEntity.ok(clienteSalvo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao cadastrar cliente: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Atualizar um cliente",
            description = "Atualiza um cliente específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
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

    @Operation(
            summary = "Deletar um cliente",
            description = "Deleta um cliente específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @DeleteMapping("/deletarCliente/{idCliente}")
    public ResponseEntity<Void> deletarCliente(@PathVariable Long idCliente) {
        boolean deletado = clienteService.deletarCliente(idCliente);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Atualizar um animal",
            description = "Atualiza um animal específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Animal atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
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

    @Operation(
            summary = "Obter opções de espécies e idades",
            description = "Retorna as opções disponíveis de espécies, raças e idades para cadastro de animais."
    )
    @ApiResponse(responseCode = "200", description = "Opções retornadas com sucesso")
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

    @Operation(
            summary = "Selecionar animal para adoção",
            description = "Marca um animal como disponível para adoção, gera um comprovante em PDF e envia por e-mail ao cliente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovante de adoção gerado e enviado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Animal ou cliente não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao gerar o comprovante de adoção")
    })
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

    @Operation(
            summary = "Cadastrar pedido de doação",
            description = "Cria um novo pedido de doação para um animal e cliente, gera um comprovante e envia por e-mail."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido de doação cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou faltando (foto ou descrição do animal)"),
            @ApiResponse(responseCode = "404", description = "Animal ou cliente não encontrado"),
            @ApiResponse(responseCode = "409", description = "Pedido de doação já existente para este animal e cliente"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar o pedido")
    })
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

    @Operation(
            summary = "Cadastrar um animal com geração de comprovante PDF",
            description = "Recebe os dados do animal e sua foto. Cadastra o animal e gera um comprovante PDF enviado por e-mail."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal cadastrado com sucesso e comprovante gerado"),
            @ApiResponse(responseCode = "500", description = "Erro ao cadastrar animal ou gerar comprovante")
    })
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

    @Operation(
            summary = "Cadastrar um animal normalmente",
            description = "Recebe os dados do animal e sua foto. Cadastra o animal no sistema sem gerar comprovante PDF."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal cadastrado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao cadastrar animal")
    })
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

    @Operation(
            summary = "Buscar animais por filtros",
            description = "Filtra animais por espécie e raça (opcionais). Retorna a lista correspondente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animais encontrados"),
            @ApiResponse(responseCode = "500", description = "Erro ao buscar animais")
    })
    //Filtro para pesquisar animal
    @GetMapping("/buscaAnimal")
    public ResponseEntity<List<AnimalModel>> buscarAnimais(
            @RequestParam(required = false) TipoEspecie especie,
            @RequestParam(required = false) String raca) {

        List<AnimalModel> animais = animalServices.buscarAnimaisPorFiltros(especie, raca);
        return ResponseEntity.ok(animais);
    }

    @Operation(
            summary = "Login do cliente",
            description = "Realiza o login do cliente a partir do e-mail e senha fornecidos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
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

    @Operation(
            summary = "Gerar token de recuperação de senha",
            description = "Gera e envia um token de recuperação de senha para o e-mail informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token enviado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao enviar token")
    })
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

    @Operation(
            summary = "Listar animais disponíveis para adoção",
            description = "Retorna a lista de animais disponíveis para adoção no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao buscar animais")
    })
    @GetMapping("/ExibirAdocoesAnimais")
    public ResponseEntity<List<AnimalModel>> getAnimaisParaAdocao() {
        List<AnimalModel> animaisParaAdocao = animalServices.getAnimaisParaAdocao();
        return new ResponseEntity<>(animaisParaAdocao, HttpStatus.OK);
    }

    @Operation(
            summary = "Listar pedidos de adoção válidos",
            description = "Retorna a lista de adoções que passaram por validação e estão aptas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Adoções válidas retornadas com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao buscar adoções")
    })
    @GetMapping("/doacao-validas")
    public ResponseEntity<List<PedidoModel>> getAdocoesDoacaoValidas() {
        List<PedidoModel> adocoes = pedidoServices.listarAdocoesDoacaoValidas();
        return new ResponseEntity<>(adocoes, HttpStatus.OK);
    }

    @Operation(
            summary = "Download de comprovante de adoção (por ID do pedido)",
            description = "Realiza o download do PDF do comprovante da adoção com base no ID do pedido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovante baixado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Comprovante não encontrado")
    })
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

    @Operation(
            summary = "Visualizar comprovante de adoção (por ID do pedido)",
            description = "Exibe o PDF do comprovante da adoção com base no ID do pedido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovante exibido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Comprovante não encontrado")
    })
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

    @Operation(
            summary = "Download/Visualizar comprovante (por ID de adoção)",
            description = "Permite visualizar ou baixar o PDF do comprovante da adoção com base no ID da adoção."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovante acessado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Comprovante não encontrado")
    })
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

    @Operation(
            summary = "Buscar comprovantes de adoção",
            description = "Busca o comprovante de adoção através do ID da adoção."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovantes retornado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao buscar comprovantes")
    })
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

    @Operation(
            summary = "Buscar comprovantes de doações por cliente",
            description = "Retorna todos os comprovantes de doações feitas pelo cliente com base no seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de comprovantes retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    @GetMapping("/doacoes/{idCliente}")
    public ResponseEntity<List<PedidoModel>> buscarComprovantesPorCliente(@PathVariable Long idCliente) {
        List<PedidoModel> adocoes = adocaoRepository.findByCliente_IdCliente(idCliente);
        return ResponseEntity.ok(adocoes);
    }

    @Operation(
            summary = "Validar token do cliente",
            description = "Verifica se o token enviado pelo cliente ainda é válido"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado")
    })
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

    @Operation(
            summary = "Atualizar senha do cliente",
            description = "Permite ao cliente atualizar sua senha usando o e-mail e a nova senha"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha atualizada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao atualizar a senha")
    })
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

    @Operation(
            summary = "Realizar denúncia",
            description = "Registra uma nova denúncia feita por um cliente, atribuindo data e status iniciais"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Denúncia registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da denúncia inválidos")
    })
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

    @Operation(
            summary = "Excluir uma denúncia",
            description = "Exclui uma denúncia com base no ID informado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Denúncia excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Denúncia não encontrada")
    })
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

    @Operation(
            summary = "Total de denúncias por cliente",
            description = "Retorna o número total de denúncias registradas por um cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
    })
    @GetMapping("/totalDenuncias/{idCliente}")
    public ResponseEntity<Long> totalDenuncias(@PathVariable Long idCliente) {
        long total = denunciaRepository.countByClienteIdCliente(idCliente); // Método que conta as denúncias pelo ID do cliente
        return ResponseEntity.ok(total);
    }

    @Operation(
            summary = "Buscar gráfico de doações por cliente",
            description = "Retorna os valores doados por um cliente para compor um gráfico de doações"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do gráfico retornados com sucesso")
    })
    @GetMapping("/graficoDoacao/{idCliente}")
    public ResponseEntity<List<DinheiroModel>> getDoacoesByCliente(@PathVariable Long idCliente) {
        List<DinheiroModel> doacoes = dinheiroRepository.findByClienteIdCliente(idCliente);
        return ResponseEntity.ok(doacoes);
    }

    @Operation(
            summary = "Contagem de denúncias por tipo",
            description = "Retorna um gráfico com o número de denúncias por tipo para um cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contagem retornada com sucesso")
    })
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

    @Operation(
            summary = "Atualizar denúncia",
            description = "Atualiza os dados de uma denúncia com base no ID informado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Denúncia atualizada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao atualizar denúncia")
    })
    @PutMapping("/AtualizarDenuncia/{idDenuncia}")
    public ResponseEntity<DenunciaModel> atualizarDenuncia(@PathVariable Long idDenuncia, @RequestBody DenunciaModel denunciaAtualizada) {
        try {
            DenunciaModel denuncia = clienteService.atualizarDenuncia(idDenuncia, denunciaAtualizada);
            return ResponseEntity.ok(denuncia);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Listar denúncias por cliente",
            description = "Retorna uma lista de denúncias feitas por um cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de denúncias retornada com sucesso")
    })
    @GetMapping("/BuscarDenuncias/{idCliente}")
    public List<DenunciaDTO> listarPorCliente(@PathVariable Long idCliente) {
        return clienteService.buscarPorCliente(idCliente);
    }

    @Operation(
            summary = "Listar denúncias por data",
            description = "Retorna todas as denúncias feitas em uma data específica"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping("/data/{dataDenuncia}")
    public List<DenunciaModel> listarPorData(@PathVariable LocalDate dataDenuncia) {
        return clienteService.buscarPorData(dataDenuncia);
    }

    @Operation(
            summary = "Listar denúncias por tipo",
            description = "Retorna todas as denúncias de um determinado tipo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    @GetMapping("/tipo/{tipoDenucias}")
    public List<DenunciaModel> listarPorTipo(@PathVariable TipoDenucias tipoDenucias) {
        return clienteService.buscarPorTipo(tipoDenucias);
    }

    @Operation(
            summary = "Deletar animal",
            description = "Remove um animal do sistema se não estiver vinculado a uma adoção"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Animal deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Animal vinculado a adoção, não pode ser deletado")
    })
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

    @Operation(
            summary = "Filtrar denúncias por cliente, data e tipo",
            description = "Busca denúncias com base no ID do cliente, data e tipo de denúncia"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtro aplicado com sucesso")
    })
    @GetMapping("/filtro")
    public List<DenunciaModel> listarPorClienteDataETipo(
            @RequestParam Long idCliente,
            @RequestParam LocalDate dataDenuncia,
            @RequestParam TipoDenucias tipoDenucias) {
        return clienteService.buscarPorClienteDataETipo(idCliente, dataDenuncia, tipoDenucias);
    }

    @Operation(
            summary = "Atualizar status de uma adoção",
            description = "Atualiza o status geral de uma adoção com base no ID e novo status"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso")
    })
    @PostMapping("/{id}/status")
    public ResponseEntity<String> atualizarStatus(@PathVariable Long id, @RequestParam StatusGeral novoStatus) {
        pedidoServices.atualizarStatus(id, novoStatus);
        return ResponseEntity.ok("Status da adoção atualizado com sucesso!");
    }

    @Operation(
            summary = "Gerar e enviar comprovante de adoção",
            description = "Gera um PDF com o comprovante de aprovação e envia por e-mail"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovante gerado e enviado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    })
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

    @Operation(
            summary = "Download do comprovante de adoção",
            description = "Retorna o PDF do comprovante de adoção do cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comprovante retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Adoção não encontrada")
    })
    @GetMapping("/comprovante/{id}")
    public ResponseEntity<byte[]> downloadComprovante(@PathVariable Long id) {
        PedidoModel adocao = adocaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Adoção não encontrada"));
        byte[] comprovante = adocao.getComprovante();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=comprovante.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(comprovante);
    }

    @Operation(
            summary = "Listar animais do cliente",
            description = "Retorna todos os animais cadastrados por um cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de animais retornada com sucesso")
    })
    @GetMapping("/ListarAnimais/{idCliente}")
    public ResponseEntity<List<AnimalModel>> getAnimaisByClienteId(@PathVariable Long idCliente) {
        List<AnimalModel> animais = animalServices.findAnimalsByIdCliente(idCliente);
        return ResponseEntity.ok(animais);
    }

    @Operation(
            summary = "Buscar animal pelo ID",
            description = "Retorna os dados do animal com base no seu ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal encontrado"),
            @ApiResponse(responseCode = "404", description = "Animal não encontrado")
    })
    @GetMapping("/ListarAnimais/Modal/{idAnimal}")
    public ResponseEntity<AnimalModel> getAnimalById(@PathVariable("idAnimal") Long idAnimal) {
        Optional<AnimalModel> animal = animalServices.findByIdAnimal(idAnimal);
        if (animal.isPresent()) {
            return ResponseEntity.ok(animal.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
            summary = "Validar documento de adoção",
            description = "Valida o status de um documento de adoção específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento validado com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro ao validar documento")
    })
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

    @Operation(
            summary = "Listar documentos do cliente",
            description = "Retorna os comprovantes de renda pendentes de um cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentos retornados com sucesso")
    })
    @GetMapping("/ListarDocumentosCliente/{idCliente}")
    public ResponseEntity<List<ComprovanteRendaModel>> listarDocumentosPendentesCliente(@PathVariable("idCliente") Long idCliente) {
        List<ComprovanteRendaModel> documentos = comprovanteRendaServices.buscarDocumentosCliente(idCliente);
        return ResponseEntity.ok(documentos);
    }

    @Operation(
            summary = "Listar documentos pendentes",
            description = "Retorna todos os comprovantes de renda pendentes no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de documentos retornada com sucesso")
    })
    // Endpoint para listar documentos pendentes
    @GetMapping("/ListarDocumentos")
    public ResponseEntity<List<ComprovanteRendaModel>> listarDocumentosPendentes() {
        List<ComprovanteRendaModel> documentos = pedidoServices.buscarDocumentos();
        return ResponseEntity.ok(documentos);
    }

    @Operation(
            summary = "Enviar documentos de comprovação de renda (versão antiga)",
            description = "Envia documentos vinculando um cliente e um animal via multipart/form-data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentos enviados com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao enviar os documentos")
    })
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

    @Operation(
            summary = "Enviar documentos de comprovação de renda",
            description = "Envia um documento PDF comprovando a renda de um cliente para um animal, verificando se já há solicitação pendente ou adoção realizada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento enviado com sucesso", content = @Content(schema = @Schema(implementation = ComprovanteRendaModel.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, animal já adotado ou já existe um pedido pendente"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar o envio do documento")
    })
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


