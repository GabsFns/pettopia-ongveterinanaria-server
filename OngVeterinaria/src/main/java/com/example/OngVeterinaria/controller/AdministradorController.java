package com.example.OngVeterinaria.controller;


import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.PedidosTipo;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import com.example.OngVeterinaria.model.Enum.TipoFuncionario;
import com.example.OngVeterinaria.repository.ComprovanteRendaRepository;
import com.example.OngVeterinaria.repository.DenunciaRepository;
import com.example.OngVeterinaria.repository.FuncionarioRepository;
import com.example.OngVeterinaria.repository.PedidoRepository;
import com.example.OngVeterinaria.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@RestController
@RequestMapping("api/adm")
public class AdministradorController implements CommandLineRunner {

    @Autowired
    private AdministradorServices administradorServices;

    @Autowired
    private ComprovanteRendaServices comprovanteRendaServices;

    @Autowired
    private ComprovanteRendaRepository comprovanteRendaRepository;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteServices clienteServices;

    @Autowired
    private FuncionarioServices funcionarioServices;

    @Autowired
    private PedidoServices pedidoServices;

    @Autowired
    private AnimalServices animalServices;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Verificando se administrador já existe...");

        // Verifica se já existe um administrador
        boolean adminExistente = funcionarioRepository.countByTipoFuncionario(TipoFuncionario.ADMINISTRADOR) > 0;

        if (!adminExistente) {
            FuncionarioModel administrador = new FuncionarioModel();
            administrador.setNome_funcionario("Administrador");
            administrador.setEmail("admin@gmail.com");
            administrador.setCpf_funcionario("363.536.264-60");
            administrador.setPasswordFuncionario(passwordEncoder.encode("admin")); // Criptografando a senha
            administrador.setTipoFuncionario(TipoFuncionario.ADMINISTRADOR);

            funcionarioRepository.save(administrador);
            System.out.println("Administrador criado com sucesso!");
        } else {
            System.out.println("Administrador já existe.");
        }
    }

    @Operation(
            summary = "Deletar um funcionário",
            description = "Deleta um funcionário específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @DeleteMapping("/deletarFuncionario/{idFuncionario}")
    public ResponseEntity<Void> deletarFuncionario(@PathVariable Long idFuncionario) {
        boolean deletado = administradorServices.deletarFuncionario(idFuncionario);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Cadastrar um funcionário",
            description = "Cadastra um funcionário"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário cadastrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao cadastrar Funcionario")
    })
    //Cadastrar Funcionario
    @PostMapping("/cadastrar/funcionario")
    public ResponseEntity<?> cadastrarFuncionario(@RequestBody FuncionarioModel funcionarioModel){
        FuncionarioModel novoFuncionario = administradorServices.cadastrarFuncionario(funcionarioModel);
        funcionarioModel.setData_emissao(LocalDate.now());
        if (novoFuncionario != null) {
           return ResponseEntity.ok(funcionarioModel);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao cadastrar Funcionario");
        }
    }

    @Operation(
            summary = "Atualizar um funcionário",
            description = "Atualiza um funcionário específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @PutMapping("/atualizarFuncionario/{id}")
    public ResponseEntity<?> atualizarFuncionario(@PathVariable Long id, @Valid @RequestBody FuncionarioModel funcionarioAtualizado) {
        // Recupera o funcionário existente no banco de dados
        Optional<FuncionarioModel> funcionarioExistenteOpt = funcionarioRepository.findById(id);

        // Se o funcionário não existir, retorna erro
        if (!funcionarioExistenteOpt.isPresent()) {
            return ResponseEntity.status(404).body("Funcionário não encontrado.");
        }

        // Chama o serviço para atualizar o funcionário
        Optional<FuncionarioModel> funcionarioAtualizadoResposta = administradorServices.atualizarFuncionario(id, funcionarioAtualizado);

        // Verifica se o funcionário foi encontrado e atualizado
        if (funcionarioAtualizadoResposta.isPresent()) {
            return ResponseEntity.ok(funcionarioAtualizadoResposta.get());
        } else {
            return ResponseEntity.status(404).body("Funcionário não encontrado.");
        }
    }

    @Operation(
            summary = "Busca um comprovante",
            description = "Busca um comprovante de renda específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comprovante encontrado"),
            @ApiResponse(responseCode = "404", description = "Comprovante não encontrado")
    })
    //Pesquisar Comprovante na tabela Desktop
    @GetMapping("/buscar/comprovante/{id}")
    public ResponseEntity<ComprovanteRendaModel> buscarComprovantePorId(@PathVariable Long id) {
        Optional<ComprovanteRendaModel> comprovante = comprovanteRendaServices.buscarComprovantePorId(id);
        return comprovante.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar funcionários",
            description = "Lista todos os funcionários"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Funcionário listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao listar funcionários")
    })
    @GetMapping("/listar/funcionario")
    public List<FuncionarioModel> listarFuncionario(){
        return funcionarioServices.listarFuncionario();
    }

    @Operation(
            summary = "Listar clientes",
            description = "Lista todos os clientes"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Clientes listados com sucesso"),
            @ApiResponse(responseCode = "404", description = "Erro ao listar clientes")
    })
    @GetMapping("/listar/cliente")
    public List<ClienteModel> listarCliente(){
        return clienteServices.listarCliente();
    }

    @Operation(
            summary = "Cancelar um pedido de doação",
            description = "Cancela um pedido de doação específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido de doação atualizado para 'CANCELADO'"),
            @ApiResponse(responseCode = "404", description = "Erro ao cancelar o pedido de doação")
    })
    @PutMapping("/cancelarPedido/{id}")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id) {
        boolean atualizado = administradorServices.cancelarPedido(id);
        return atualizado ? ResponseEntity.ok("Pedido de doação atualizado para 'CANCELADO'") : ResponseEntity.status(404).body("Pedido de doação não encontrado.");
    }

    @Operation(
            summary = "Aceita um pedido de doação",
            description = "Cancela um pedido de doação específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido de doação atualizado para 'ANDAMENTO'"),
            @ApiResponse(responseCode = "404", description = "Erro ao cancelar pedido de doação")
    })
    @PutMapping("/aceitarPedido/{id}")
    public ResponseEntity<?> aceitarPedido(@PathVariable Long id) {
        boolean atualizado = administradorServices.aceitarPedido(id);
        return atualizado ? ResponseEntity.ok("Pedido de doação atualizado para 'ANDAMENTO'") : ResponseEntity.status(404).body("Pedido de doação não encontrado.");
    }

    @Operation(
            summary = "Buscar cliente",
            description = "Busca um cliente específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    //Pesquisar Usuario na tabela Desktop
    @GetMapping("/cliente/{id}")
    public ResponseEntity<ClienteModel> buscarClientePorId(@PathVariable Long id) {
        Optional<ClienteModel> cliente = clienteServices.buscarClientePorId(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Buscar funcionário",
            description = "Busca um funcionário específico com base no ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "funcionário encontrado"),
            @ApiResponse(responseCode = "404", description = "funcionário não encontrado")
    })
    //Pesquisar Usuario na tabela Desktop
    @GetMapping("/buscarFuncionario/{id}")
    public ResponseEntity<FuncionarioModel> buscarFuncionarioPorId(@PathVariable Long id) {
        Optional<FuncionarioModel> funcionario = funcionarioServices.findByIdFuncionario(id);
        return funcionario.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Método agendado para rodar a cada 24 horas (ajuste conforme necessário)
    @Scheduled(fixedRate = 86400000) // 24 horas em milissegundos
    public void excluirArquivosExpirados() {
        LocalDateTime agora = LocalDateTime.now();

        // Busca documentos com tempoExclusao expirado
        List<ComprovanteRendaModel> documentosExpirados = comprovanteRendaRepository
                .findByStatusAndTempoExclusaoBefore(StatusGeral.CANCELADO, agora);

        for (ComprovanteRendaModel documento : documentosExpirados) {
            // Exclui o arquivo (setando como null ou outra lógica desejada)
            documento.setArquivo(null);

            // Salva novamente o documento sem o arquivo
            comprovanteRendaRepository.save(documento);
        }
    }

    @Operation(
            summary = "Buscar comprovante",
            description = "Busca um comprovante específico com base no ID e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comprovante encontrado"),
            @ApiResponse(responseCode = "404", description = "Comprovante não encontrado")
    })
    // Endpoint para buscar o comprovante por ID e retornar como PDF
    @GetMapping("/comprovante/{id}")
    public ResponseEntity<byte[]> baixarComprovante(@PathVariable Long id) {
        return comprovanteRendaRepository.findById(id)
                .filter(doc -> doc.getTempoExclusao() == null || doc.getTempoExclusao().isAfter(LocalDateTime.now()))
                .map(doc -> {
                    // Configura o cabeçalho e o corpo da resposta
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDisposition(ContentDisposition.builder("inline")
                            .filename("comprovante-" + id + ".pdf")
                            .build());

                    return ResponseEntity.ok()
                            .headers(headers)
                            .body(doc.getArquivo());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Validar comprovante de renda",
            description = "Validar comprovante de renda específico com base no ID e retorna o comprovante do pedido de adoção como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comprovante validado"),
            @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    })
    @PutMapping("/validarComprovante/{idDocumento}")
    public PedidoModel validarAdocao(@PathVariable Long idDocumento) throws Exception {
        // Busca o comprovante pelo ID
        ComprovanteRendaModel documento = comprovanteRendaRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("Documento não encontrado"));

        documento.setStatus(StatusGeral.CONCLUIDO);

        // Calcula o tempo para excluir o comprovante (3 dias após a validação)
        documento.setTempoExclusao(LocalDateTime.now().plusDays(3));

        // Gera o comprovante e adiciona ao registro da adoção
        String codigoComprovacao = UUID.randomUUID().toString();
        PedidoModel adocao = new PedidoModel();
        adocao.setCliente(documento.getCliente());
        adocao.setAnimal(documento.getAnimal());
        adocao.setTipo(PedidosTipo.ADOCAO);  // Alterado para ADOCAO
        adocao.setStatusPedido(StatusGeral.ANDAMENTO);
        adocao.setValido(true);
        adocao.setCodigoComprovante(codigoComprovacao);

        // Gera o PDF do comprovante
        byte[] comprovante = animalServices.gerarComprovantePDFAdocao(adocao.getAnimal(), adocao.getCliente(), codigoComprovacao);
        adocao.setComprovante(comprovante);

        comprovanteRendaRepository.save(documento);

        // Salva o pedido de adoção
        pedidoRepository.save(adocao);

        // Envia o comprovante por e-mail
        try {
            animalServices.enviarComprovanteComPDF(
                    documento.getCliente().getEmail(),
                    comprovante,
                    adocao.getCodigoComprovante()
            );
        } catch (MessagingException e) {
            throw new Exception("Erro ao enviar o comprovante por email", e);
        }

        // Retorna o pedido de adoção criado
        return adocao;
    }

    @Operation(
            summary = "Negar comprovante",
            description = "Nega um comprovante específico com base no ID e defini um período para exclusão"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comprovante negado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Comprovante não negado")
    })
    @PutMapping("/negarComprovante/{idDocumento}")
    public ComprovanteRendaModel negarComprovante(@PathVariable Long idDocumento) throws Exception {
        // Busca o comprovante pelo ID
        ComprovanteRendaModel documento = comprovanteRendaRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("Documento não encontrado"));

        // Verifica se o status não está já como CANCELADO
        if (documento.getStatus() == StatusGeral.CANCELADO) {
            throw new Exception("O documento já está cancelado.");
        }

        // Atualiza o status do documento
        documento.setStatus(StatusGeral.CANCELADO);

        // Define o tempo de exclusão para 3 dias após o cancelamento
        documento.setTempoExclusao(LocalDateTime.now().plusDays(3));

        // Salva o documento com o novo status e tempo de exclusão
        return comprovanteRendaRepository.save(documento);
    }

    @Operation(
            summary = "Gerar relatorio de doações",
            description = "Gera um relatório contendo todos os pedidos de doação e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @PostMapping("/gerarRelatorioDoacoes")
    public ResponseEntity<DoacaoRelatorioDTO> gerarRelatorioDoacoes(@RequestBody DoacaoRelatorioDTO relatorioDTO) {
        try {
            // Gera o relatório e atualiza o DTO
            DoacaoRelatorioDTO relatorioGerado = administradorServices.gerarRelatorioDoacoes(relatorioDTO);

            // Retorna o DTO atualizado
            return ResponseEntity.ok(relatorioGerado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Gerar relatorio de adoções",
            description = "Gera um relatório contendo todos os pedidos de adoção e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @PostMapping("/gerarRelatorioAdocoes")
    public ResponseEntity<AdocaoRelatorioDTO> gerarRelatorioAdocoes(@RequestBody AdocaoRelatorioDTO relatorioDTO) {
        try {
            // Gera o relatório e atualiza o DTO
            AdocaoRelatorioDTO relatorioGerado = administradorServices.gerarRelatorioAdocoes(relatorioDTO);

            // Retorna o DTO atualizado
            return ResponseEntity.ok(relatorioGerado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Gerar relatorio de denúncias",
            description = "Gera um relatório contendo todos os pedidos de denúncia e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @PostMapping("/gerarRelatorioDenuncias")
    public ResponseEntity<DenunciaRelatorioDTO> gerarRelatorioDenuncias(@RequestBody DenunciaRelatorioDTO relatorioDTO) {
        try {
            // Gera o relatório e atualiza o DTO
            DenunciaRelatorioDTO relatorioGerado = administradorServices.gerarRelatorioDenuncias(relatorioDTO);

            // Retorna o DTO atualizado
            return ResponseEntity.ok(relatorioGerado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Gerar relatorio de animais cadastrados",
            description = "Gera um relatório contendo todos os animais cadastrados e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @PostMapping("/gerarRelatorioAnimais")
    public ResponseEntity<AnimalRelatorioDTO> gerarRelatorioAnimais(@RequestBody AnimalRelatorioDTO relatorioDTO) {
        try {
            // Gera o relatório e atualiza o DTO
            AnimalRelatorioDTO relatorioGerado = administradorServices.gerarRelatorioAnimais(relatorioDTO);

            // Retorna o DTO atualizado
            return ResponseEntity.ok(relatorioGerado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Gerar relatorio de clientes cadastrados",
            description = "Gera um relatório contendo todos os clientes cadastrados e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @PostMapping("/gerarRelatorioClientes")
    public ResponseEntity<ClienteRelatorioDTO> gerarRelatorioClientes(@RequestBody ClienteRelatorioDTO relatorioDTO) {
        try {
            // Gera o relatório e atualiza o DTO
            ClienteRelatorioDTO relatorioGerado = administradorServices.gerarRelatorioClientes(relatorioDTO);

            // Retorna o DTO atualizado
            return ResponseEntity.ok(relatorioGerado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(
            summary = "Gerar relatorio de funcionários cadastrados",
            description = "Gera um relatório contendo todos os funcionários cadastrados e retorna como PDF"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @PostMapping("/gerarRelatorioFuncionarios")
    public ResponseEntity<FuncionarioRelatorioDTO> gerarRelatorioFuncionarios(@RequestBody FuncionarioRelatorioDTO relatorioDTO) {
        try {
            // Gera o relatório e atualiza o DTO
            FuncionarioRelatorioDTO relatorioGerado = administradorServices.gerarRelatorioFuncionarios(relatorioDTO);

            // Retorna o DTO atualizado
            return ResponseEntity.ok(relatorioGerado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Operation(
            summary = "Gerar gráficos de adoção, denúncia e doações",
            description = "Gera três gráficos (adoção/doação de animais, denúncias registradas e doações em dinheiro), converte-os para imagens Base64 e retorna como um JSON."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Relatório não gerado")
    })
    @GetMapping("/grafico")
    public ResponseEntity<Map<String, String>> obterGrafico() throws IOException {
        // Gerar os gráficos
        BufferedImage grafico1 = administradorServices.gerarGraficoAdocaoDoacao();
        BufferedImage grafico2 = administradorServices.gerarGraficoDenuncia();
        BufferedImage grafico3 = administradorServices.gerarGraficoDoacoesDinheiro();
        // Converter o primeiro gráfico para bytes e depois para Base64
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ImageIO.write(grafico1, "png", baos1);
        byte[] imagemBytes1 = baos1.toByteArray();
        String imagemBase64_1 = Base64.getEncoder().encodeToString(imagemBytes1);

        // Converter o segundo gráfico para bytes e depois para Base64
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        ImageIO.write(grafico2, "png", baos2);
        byte[] imagemBytes2 = baos2.toByteArray();
        String imagemBase64_2 = Base64.getEncoder().encodeToString(imagemBytes2);

        ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
        ImageIO.write(grafico3, "png", baos3);
        byte[] imagemBytes3 = baos3.toByteArray();
        String imagemBase64_3 = Base64.getEncoder().encodeToString(imagemBytes3);

        // Criar um mapa com as imagens em Base64
        Map<String, String> imagens = new HashMap<>();
        imagens.put("grafico1", imagemBase64_1);
        imagens.put("grafico2", imagemBase64_2);
        imagens.put("grafico3", imagemBase64_3);

        // Retornar as imagens como resposta
        return new ResponseEntity<>(imagens, HttpStatus.OK);
    }
}
