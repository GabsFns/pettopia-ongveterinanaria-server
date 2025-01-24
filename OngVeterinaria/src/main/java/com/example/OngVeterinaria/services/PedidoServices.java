package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.PedidoModel;
import com.example.OngVeterinaria.model.ComprovanteRendaModel;
import com.example.OngVeterinaria.model.Enum.PedidosTipo;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.repository.PedidoRepository;
import com.example.OngVeterinaria.repository.AnimalRepository;
import com.example.OngVeterinaria.repository.ClienteRepository;
import com.example.OngVeterinaria.repository.ComprovanteRendaRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PedidoServices {

    @Autowired
    private PedidoRepository adocaoRepository;

    @Autowired
    private ComprovanteRendaRepository comprovanteRendaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private JavaMailSender mailSender;

    @PostConstruct
    public void init() {
        // Configurações iniciais se necessário
    }

    public PedidoModel validarAdocao(Long idDocumento, StatusGeral status) throws Exception {
        ComprovanteRendaModel documento = comprovanteRendaRepository.findById(idDocumento)
                .orElseThrow(() -> new Exception("Documento não encontrado"));

        // Atualiza o status do documento
        documento.setStatus(status);
        comprovanteRendaRepository.save(documento);

        // Declaração de adocao como null apenas quando status não é "CONCLUIDO"
        if (status == StatusGeral.CONCLUIDO) {
            PedidoModel adocao = new PedidoModel();
            adocao.setCliente(documento.getCliente());
            adocao.setAnimal(documento.getAnimal());
            adocao.setStatusPedido(StatusGeral.PENDENTE);
            adocao.setComprovante(gerarComprovanteAprovacao(adocao));

            adocaoRepository.save(adocao);
            return adocao;  // Retorna a adoção criada
        }

        return null;  // Se não houver adoção criada, retorna null
    }

    public PedidoModel alterarStatus(long idAdocao, StatusGeral status) {
        PedidoModel adocao = adocaoRepository.findById(idAdocao)
                .orElseThrow(() -> new IllegalArgumentException("Adoção não encontrada"));
        adocao.setStatusPedido(status);
        return adocaoRepository.save(adocao);
    }

    public void solicitarAdocao(PedidoModel adocao) {
        adocao.setStatusPedido(StatusGeral.PENDENTE);
        adocaoRepository.save(adocao);
    }

    public void atualizarStatus(long id, StatusGeral novoStatus) {
        PedidoModel adocao = adocaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adoção não encontrada"));
        adocao.setStatusPedido(novoStatus);
        adocaoRepository.save(adocao);
    }

    public List<PedidoModel> listarAdocoesDoacaoValidas() {
        return adocaoRepository.findByTipo(PedidosTipo.DOACAO);
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

    public PedidoModel buscarAdocaoPorId(Long idAdocao) {
        // Lógica para buscar adoção pelo ID
        return adocaoRepository.findById(idAdocao)
                .orElseThrow(() -> new ResourceNotFoundException("Adoção não encontrada"));
    }

    public List<ComprovanteRendaModel> buscarDocumentos() {
        return comprovanteRendaRepository.findAll();
    }

    public byte[] gerarComprovanteAprovacao(PedidoModel adocao) {
        // Aqui você deve criar o PDF usando uma biblioteca como iText ou Apache PDFBox
        // Aqui está um exemplo básico com iText:
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph("Comprovante de Adoção"));
            document.add(new Paragraph("ID da Adoção: " + adocao.getIdPedido()));
            document.add(new Paragraph("Cliente: " + adocao.getCliente().getNome()));
            document.add(new Paragraph("Animal: " + adocao.getAnimal().getNome()));
            document.add(new Paragraph("Status: " + adocao.getStatusPedido()));
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public void enviarEmailComprovante(String emailDestino, byte[] comprovante) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestino);
        message.setSubject("Confirmação de Adoção");
        message.setText("Sua adoção foi aprovada! Anexamos o comprovante de adoção.");

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(emailDestino);
        helper.setSubject("Confirmação de Adoção");
        helper.setText("Sua adoção foi aprovada! Anexamos o comprovante de adoção.");
        helper.addAttachment("comprovante_adoção.pdf", new ByteArrayDataSource(comprovante, "application/pdf"));
        mailSender.send(mimeMessage);
    }
}
