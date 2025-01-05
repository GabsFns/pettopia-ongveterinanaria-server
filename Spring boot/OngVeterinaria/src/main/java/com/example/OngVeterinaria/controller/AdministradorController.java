package com.example.OngVeterinaria.controller;


import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.PedidosTipo;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import com.example.OngVeterinaria.repository.ComprovanteRendaRepository;
import com.example.OngVeterinaria.repository.DenunciaRepository;
import com.example.OngVeterinaria.repository.FuncionarioRepository;
import com.example.OngVeterinaria.repository.PedidoRepository;
import com.example.OngVeterinaria.services.*;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
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
public class AdministradorController {

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

    @DeleteMapping("/deletarFuncionario/{idFuncionario}")
    public ResponseEntity<Void> deletarFuncionario(@PathVariable Long idFuncionario) {
        boolean deletado = administradorServices.deletarFuncionario(idFuncionario);
        return deletado ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

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

    //Pesquisar Comprovante na tabela Desktop
    @GetMapping("/buscar/comprovante/{id}")
    public ResponseEntity<ComprovanteRendaModel> buscarComprovantePorId(@PathVariable Long id) {
        Optional<ComprovanteRendaModel> comprovante = comprovanteRendaServices.buscarComprovantePorId(id);
        return comprovante.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/listar/funcionario")
    public List<FuncionarioModel> listarFuncionario(){
        return funcionarioServices.listarFuncionario();
    }

    @GetMapping("/listar/cliente")
    public List<ClienteModel> listarCliente(){
        return clienteServices.listarCliente();
    }

    @PutMapping("/cancelarPedido/{id}")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id) {
        boolean atualizado = administradorServices.cancelarPedido(id);
        return atualizado ? ResponseEntity.ok("Pedido de doação atualizado para 'CANCELADO'") : ResponseEntity.status(404).body("Pedido de doação não encontrado.");
    }

    @PutMapping("/aceitarPedido/{id}")
    public ResponseEntity<?> aceitarPedido(@PathVariable Long id) {
        boolean atualizado = administradorServices.aceitarPedido(id);
        return atualizado ? ResponseEntity.ok("Pedido atualizado para 'ANDAMENTO'") : ResponseEntity.status(404).body("Pedido de doação não encontrado.");
    }

    //Pesquisar Usuario na tabela Desktop
    @GetMapping("/cliente/{id}")
    public ResponseEntity<ClienteModel> buscarClientePorId(@PathVariable Long id) {
        Optional<ClienteModel> cliente = clienteServices.buscarClientePorId(id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

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
