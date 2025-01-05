package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.PedidoModel;
import com.example.OngVeterinaria.model.AnimalModel;
import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.Enum.*;
import com.example.OngVeterinaria.repository.PedidoRepository;
import com.example.OngVeterinaria.repository.AnimalRepository;
import com.example.OngVeterinaria.repository.ClienteRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AnimalServices {

        @Autowired
        private AnimalRepository animalRepository;

        @Autowired
        private ClienteRepository clienteRepository;

         @Autowired
         private PedidoRepository adocaoRepository;

        @Autowired
        private JavaMailSender mailSender;


    public AnimalModel cadastrarAnimal(ClienteModel cliente, String nome, TipoEspecie especie, Genero sexo, String raca, String idade, String cor, double peso, byte[] foto, String descricao, boolean adocao) {
        // Validação da raça
        if (!isValidRaca(especie, raca)) {
            throw new RuntimeException("Raça inválida para a espécie informada");
        }

        // Cria uma nova instância de AnimalModel
        AnimalModel animal = new AnimalModel();
        animal.setNome(nome);
        animal.setEspecie(especie);
        animal.setSexo(sexo);
        animal.setRaca(raca);
        animal.setIdade(idade);
        animal.setCor(cor);
        animal.setPeso(peso);
        animal.setCliente(cliente); // Atribui o cliente diretamente

        if (foto != null && foto.length > 0) {
            animal.setFotoAnimal(foto);
        } else {
            animal.setFotoAnimal(new byte[0]); // Ou outra lógica para tratamento de nulo, se aplicável
        }

        if (descricao != null) {
            animal.setDescricao(descricao);
        }

        // Define se o animal está para adoção
        animal.setAdocao(adocao);

        // Salva o animal no banco de dados
        return animalRepository.save(animal);
    }

    public byte[] gerarComprovantePDF(AnimalModel animal, ClienteModel cliente, String codigo) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Define as cores
        BaseColor primaryColor = new BaseColor(76, 175, 172); // #4cafac
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;

        // Define fontes
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, blackColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Adiciona o logo e o endereço da ONG
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3}); // Largura das colunas

        // Logo
        PdfPCell logoCell = new PdfPCell();
        Image logo = Image.getInstance("src/main/resources/images/iconPettopia.png");
        logo.scaleToFit(120, 120);
        logoCell.addElement(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        headerTable.addCell(logoCell);

        // Endereço da ONG
        PdfPCell enderecoCell = new PdfPCell();
        enderecoCell.setBorder(Rectangle.NO_BORDER);
        enderecoCell.addElement(new Paragraph("ONG Pettopia", headerFont));
        enderecoCell.addElement(new Paragraph("Estr. Mal. Alencastro, s/n, Quadra B", bodyFont));
        enderecoCell.addElement(new Paragraph("Ricardo de Albuquerque, Rio de Janeiro - RJ, 21615-320", bodyFont));
        enderecoCell.addElement(new Paragraph("Telefone: (21) 99265-2607", bodyFont));
        headerTable.addCell(enderecoCell);

        document.add(headerTable);

        // Linha separadora
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título
        Paragraph title = new Paragraph("Comprovante de Cadastro de Doção", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20); // Espaço abaixo do título
        document.add(title);

        // Seção de dados do cliente
        PdfPTable clienteTable = new PdfPTable(1);
        clienteTable.setWidthPercentage(100);

        PdfPCell clienteCell = new PdfPCell();
        clienteCell.setBorder(Rectangle.BOX);
        clienteCell.setPadding(10);
        clienteCell.addElement(new Paragraph("Dados do Cliente", headerFont));
        clienteCell.addElement(new Paragraph("Nome: " + cliente.getNome(), bodyFont));
        clienteCell.addElement(new Paragraph("CPF: " + cliente.getCpf(), bodyFont));
        clienteCell.addElement(new Paragraph("Email: " + cliente.getEmail(), bodyFont));
        clienteCell.addElement(new Paragraph("Telefone: " + cliente.getTelefone(), bodyFont));
        clienteCell.setPaddingBottom(15); // Espaço após os dados do cliente
        clienteTable.addCell(clienteCell);

        document.add(clienteTable);

        document.add(new Paragraph(" "));

        PdfPTable AnimalTable = new PdfPTable(1);
        AnimalTable.setWidthPercentage(100);

        PdfPCell AnimalCell = new PdfPCell();
        AnimalCell.setBorder(Rectangle.BOX);
        AnimalCell.setPadding(10);

        AnimalCell.addElement(new Paragraph("Dados do Animal", headerFont));
        AnimalCell.addElement(new Paragraph("Nome: " + animal.getNome(), bodyFont));
        AnimalCell.addElement(new Paragraph("Espécie: " + animal.getEspecie(), bodyFont));
        AnimalCell.addElement(new Paragraph("Raça: " + animal.getRaca(), bodyFont));
        AnimalCell.addElement(new Paragraph("Sexo: " + animal.getSexo(), bodyFont));
        AnimalCell.addElement(new Paragraph("Idade: " + animal.getIdade(), bodyFont));
        AnimalCell.addElement(new Paragraph("Peso: " + animal.getPeso(), bodyFont));
        AnimalCell.addElement(new Paragraph("Código: " + animal.getIdAnimal(), bodyFont));
        AnimalCell.setPaddingBottom(15); // Espaço após os dados do cliente
        AnimalTable.addCell(AnimalCell);

        document.add(AnimalTable);

        // Espaço
        document.add(new Paragraph(" "));

        // Seção de unidade de adoção
        PdfPTable unidadeTable = new PdfPTable(1);
        unidadeTable.setWidthPercentage(100);

        PdfPCell unidadeCell = new PdfPCell();
        unidadeCell.setBorder(Rectangle.BOX);
        unidadeCell.setPadding(10);

        unidadeCell.addElement(new Paragraph("Unidade para realizar a Doação", headerFont));
        unidadeCell.addElement(new Paragraph("Unidade: Centro de Adoção Pettopia", bodyFont));
        unidadeCell.addElement(new Paragraph("Endereço: Av. dos Animais, 5678, Cidade Verde - SP", bodyFont));
        unidadeCell.setPaddingBottom(15); // Espaço após a unidade
        unidadeTable.addCell(unidadeCell);

        document.add(unidadeTable);

        // Seção de termos de adoção
        Paragraph termos = new Paragraph("Ao realizar o cadastro, você se compromete a realizar a Doação em até 10 dias. Após esse prazo, o cadastro será cancelado. Para concluir a adoção, compareça à unidade mencionada com este comprovante.", bodyFont);
        termos.setAlignment(Element.ALIGN_CENTER);
        termos.setSpacingBefore(20); // Espaço antes dos termos
        termos.setSpacingAfter(20);  // Espaço depois dos termos
        document.add(termos);

        // Código de comprovação e validade
        Paragraph codeParagraph = new Paragraph("Código de Comprovação: " + codigo, bodyFont);
        codeParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(codeParagraph);

        Paragraph validityParagraph = new Paragraph("Validade: 10 dias", bodyFont);
        validityParagraph.setAlignment(Element.ALIGN_CENTER);
        validityParagraph.setSpacingAfter(20); // Espaço após a validade
        document.add(validityParagraph);

        // Adiciona o código de barras
        BufferedImage barcodeImage = generateBarcodeImage(codigo); // Função para gerar código de barras

// Converte o BufferedImage para o formato que o iText aceita
        ByteArrayOutputStream baosBarcode = new ByteArrayOutputStream();
        ImageIO.write(barcodeImage, "png", baosBarcode);
        Image barcodePdfImage = Image.getInstance(baosBarcode.toByteArray());

// Ajuste o tamanho e posição do código de barras no PDF
        barcodePdfImage.scaleToFit(200, 50); // Ajusta o tamanho conforme necessário
        barcodePdfImage.setAlignment(Element.ALIGN_CENTER); // Alinha o código de barras

// Adiciona o código de barras ao documento
        document.add(barcodePdfImage);

        // Linha separadora final
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Rodapé
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 10);
        cb.setColorFill(primaryColor);
        cb.showTextAligned(Element.ALIGN_CENTER, "Doação responsável é um ato de amor! ONG Pettopia", 297.5f, 20, 0);
        cb.endText();
        cb.restoreState();

        addMarcaDagua(writer, "Pettopia ONG - Confidencial");
        // Finaliza o documento
        document.close();

        PedidoModel adocao = new PedidoModel();
        adocao.setAnimal(animal);
        adocao.setCliente(cliente);
        adocao.setCodigoComprovante(codigo);
        adocao.setComprovante(baos.toByteArray());
        adocao.setTipo(PedidosTipo.DOACAO);
        adocao.setStatusPedido(StatusGeral.PENDENTE);

        // Salva a adoção no banco de dados
        adocaoRepository.save(adocao);
        return baos.toByteArray(); // Retorna o PDF como um array de bytes
    }

    public void addMarcaDagua(PdfWriter writer, String watermarkText) {
        PdfContentByte canvas = writer.getDirectContentUnder();

        // Configura a transparência
        PdfGState gstate = new PdfGState();
        gstate.setFillOpacity(0.1f); // Define a opacidade da marca d'água (0.1 é bem transparente)

        // Define a fonte da marca d'água
        Font font = new Font(Font.FontFamily.HELVETICA, 52, Font.BOLD, BaseColor.GRAY);
        Phrase phrase = new Phrase(watermarkText, font);

        // Obtem as dimensões da página
        Rectangle rect = writer.getPageSize();

        // Posiciona e rotaciona o texto da marca d'água
        canvas.saveState();
        canvas.setGState(gstate);
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, phrase,
                (rect.getLeft() + rect.getRight()) / 2,  // X: Centro horizontal
                (rect.getTop() + rect.getBottom()) / 2, // Y: Centro vertical
                45); // Rotaciona em 45 graus
        canvas.restoreState();
    }

    // Função para gerar o código de barras
    public BufferedImage generateBarcodeImage(String code) throws IOException {
        Code128Bean barcodeGenerator = new Code128Bean();
        final int dpi = 160;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(baos, "image/x-png", dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);

        barcodeGenerator.generateBarcode(canvas, code);
        canvas.finish();

        return canvas.getBufferedImage();
    }

    public byte[] gerarComprovantePDFAdocao(AnimalModel animal, ClienteModel cliente, String codigo) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Define as cores
        BaseColor primaryColor = new BaseColor(76, 175, 172); // #4cafac
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;

        // Define fontes
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, blackColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Adiciona o logo e o endereço da ONG
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3}); // Largura das colunas

        // Logo
        PdfPCell logoCell = new PdfPCell();
        Image logo = Image.getInstance("src/main/resources/images/iconPettopia.png");
        logo.scaleToFit(120, 120);
        logoCell.addElement(logo);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        headerTable.addCell(logoCell);

        // Endereço da ONG
        PdfPCell enderecoCell = new PdfPCell();
        enderecoCell.setBorder(Rectangle.NO_BORDER);
        enderecoCell.addElement(new Paragraph("ONG Pettopia", headerFont));
        enderecoCell.addElement(new Paragraph("Rua das Flores, 1234", bodyFont));
        enderecoCell.addElement(new Paragraph("Bairro Esperança, Cidade Verde - SP", bodyFont));
        enderecoCell.addElement(new Paragraph("Telefone: (11) 9999-9999", bodyFont));
        headerTable.addCell(enderecoCell);

        document.add(headerTable);

        // Linha separadora
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título
        Paragraph title = new Paragraph("Comprovante de Cadastro para Adoção", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20); // Espaço abaixo do título
        document.add(title);

        // Seção de dados do cliente
        PdfPTable clienteTable = new PdfPTable(1);
        clienteTable.setWidthPercentage(100);

        PdfPCell clienteCell = new PdfPCell();
        clienteCell.setBorder(Rectangle.BOX);
        clienteCell.setPadding(10);
        clienteCell.addElement(new Paragraph("Dados do Cliente", headerFont));
        clienteCell.addElement(new Paragraph("Nome: " + cliente.getNome(), bodyFont));
        clienteCell.addElement(new Paragraph("CPF: " + cliente.getCpf(), bodyFont));
        clienteCell.addElement(new Paragraph("Email: " + cliente.getEmail(), bodyFont));
        clienteCell.addElement(new Paragraph("Telefone: " + cliente.getTelefone(), bodyFont));
        clienteCell.setPaddingBottom(15); // Espaço após os dados do cliente
        clienteTable.addCell(clienteCell);

        document.add(clienteTable);

        document.add(new Paragraph(" "));

        PdfPTable AnimalTable = new PdfPTable(1);
        AnimalTable.setWidthPercentage(100);

        PdfPCell AnimalCell = new PdfPCell();
        AnimalCell.setBorder(Rectangle.BOX);
        AnimalCell.setPadding(10);

        AnimalCell.addElement(new Paragraph("Dados do Animal", headerFont));
        AnimalCell.addElement(new Paragraph("Nome: " + animal.getNome(), bodyFont));
        AnimalCell.addElement(new Paragraph("Espécie: " + animal.getEspecie(), bodyFont));
        AnimalCell.addElement(new Paragraph("Raça: " + animal.getRaca(), bodyFont));
        AnimalCell.addElement(new Paragraph("Sexo: " + animal.getSexo(), bodyFont));
        AnimalCell.addElement(new Paragraph("Idade: " + animal.getIdade(), bodyFont));
        AnimalCell.addElement(new Paragraph("Peso: " + animal.getPeso(), bodyFont));
        AnimalCell.addElement(new Paragraph("Código: " + animal.getIdAnimal(), bodyFont));
        AnimalCell.setPaddingBottom(15); // Espaço após os dados do animal
        AnimalTable.addCell(AnimalCell);

        document.add(AnimalTable);

        // Espaço
        document.add(new Paragraph(" "));

        // Seção de unidade de adoção
        PdfPTable unidadeTable = new PdfPTable(1);
        unidadeTable.setWidthPercentage(100);

        PdfPCell unidadeCell = new PdfPCell();
        unidadeCell.setBorder(Rectangle.BOX);
        unidadeCell.setPadding(10);

        unidadeCell.addElement(new Paragraph("Unidade para realizar a Adoção", headerFont));
        unidadeCell.addElement(new Paragraph("Unidade: Centro de Adoção Pettopia", bodyFont));
        unidadeCell.addElement(new Paragraph("Endereço: Av. dos Animais, 5678, Cidade Verde - SP", bodyFont));
        unidadeCell.setPaddingBottom(15); // Espaço após a unidade
        unidadeTable.addCell(unidadeCell);

        document.add(unidadeTable);

        // Seção de termos de adoção
        Paragraph termos = new Paragraph("Ao realizar o cadastro, você se compromete a realizar a Adoção em até 10 dias. Após esse prazo, o cadastro será cancelado. Para concluir a adoção, compareça à unidade mencionada com este comprovante.", bodyFont);
        termos.setAlignment(Element.ALIGN_CENTER);
        termos.setSpacingBefore(20); // Espaço antes dos termos
        termos.setSpacingAfter(20);  // Espaço depois dos termos
        document.add(termos);

        // Código de comprovação e validade
        Paragraph codeParagraph = new Paragraph("Código de Comprovação: " + codigo, bodyFont);
        codeParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(codeParagraph);

        Paragraph validityParagraph = new Paragraph("Validade: 10 dias", bodyFont);
        validityParagraph.setAlignment(Element.ALIGN_CENTER);
        validityParagraph.setSpacingAfter(20); // Espaço após a validade
        document.add(validityParagraph);

        // Adiciona o código de barras
        BufferedImage barcodeImage = generateBarcodeImage(codigo); // Função para gerar código de barras

        // Converte o BufferedImage para o formato que o iText aceita
        ByteArrayOutputStream baosBarcode = new ByteArrayOutputStream();
        ImageIO.write(barcodeImage, "png", baosBarcode);
        Image barcodePdfImage = Image.getInstance(baosBarcode.toByteArray());

        // Ajuste o tamanho e posição do código de barras no PDF
        barcodePdfImage.scaleToFit(200, 50); // Ajusta o tamanho conforme necessário
        barcodePdfImage.setAlignment(Element.ALIGN_CENTER); // Alinha o código de barras

        // Adiciona o código de barras ao documento
        document.add(barcodePdfImage);

        // Linha separadora final
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Rodapé
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        cb.beginText();
        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED), 10);
        cb.setColorFill(primaryColor);
        cb.showTextAligned(Element.ALIGN_CENTER, "Adoção responsável é um ato de amor! ONG Pettopia", 297.5f, 20, 0);
        cb.endText();
        cb.restoreState();

        addMarcaDagua(writer, "Pettopia ONG - Confidencial");
        // Finaliza o documento
        document.close();

//        PedidoModel adocao = new PedidoModel();
//        adocao.setAnimal(animal);
//        adocao.setCliente(cliente);
//        adocao.setCodigoComprovante(codigo);
//        adocao.setComprovante(baos.toByteArray());
//        adocao.setTipo(PedidosTipo.ADOCAO);  // Alterado para ADOCAO
//        adocao.setStatusPedido(StatusGeral.PENDENTE);
//
//        // Salva a adoção no banco de dados
//        adocaoRepository.save(adocao);
        return baos.toByteArray(); // Retorna o PDF como um array de bytes
    }

    public void enviarComprovanteComPDF(String emailDestinatario, byte[] pdfBytes, String nomeArquivo) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(emailDestinatario);
        helper.setSubject("Comprovante de Cadastro de Doção");
        helper.setText("Segue em anexo o comprovante do cadastro de Doção. Por favor, leve o animal ao estabelecimento em até 10 dias.");

        // Anexar o PDF ao email
        helper.addAttachment(nomeArquivo, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }

    public List<AnimalModel> getAnimaisParaAdocao() {
        return animalRepository.findByAdocaoTrue();
    }

    private boolean isValidRaca(TipoEspecie especie, String raca) {
        // Valida a raça com base na espécie
        Class<? extends Enum<?>> racaEnum = especie.getRacaEnum();
        for (Enum<?> enumValue : racaEnum.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(raca)) {
                return true;
            }
        }
        return false;
    }

    // Método para deletar um animal pelo ID
    public Optional<AnimalModel> deletarAnimal(Long idAnimal) {
        // Busca o animal pelo ID
        Optional<AnimalModel> animal = animalRepository.findById(idAnimal);
        if (animal.isPresent()) {
            // Se o animal existe, deleta-o
            animalRepository.deleteById(idAnimal);
        }
        return animal; // Retorna o animal deletado, ou Optional.empty() se não foi encontrado
    }

    // Método para atualizar os dados de um animal existente
    public Optional<AnimalModel> atualizarAnimal(Long idAnimal, String nome, TipoEspecie especie, String idade, String cor, double peso, byte[] foto, String descricao) {
        // Busca o animal existente pelo ID
        Optional<AnimalModel> optionalAnimal = animalRepository.findById(idAnimal);

        if (optionalAnimal.isPresent()) {
            AnimalModel animal = optionalAnimal.get();

            // Atualiza os campos do animal, se os novos valores forem fornecidos
            if (nome != null && !nome.isEmpty()) {
                animal.setNome(nome);
            }
            if (especie != null) {
                animal.setEspecie(especie);
            }
            if (idade != null) {
                animal.setIdade(idade);
            }
            if (cor != null && !cor.isEmpty()) {
                animal.setCor(cor);
            }
            if (peso > 0) {
                animal.setPeso(peso);
            }
            if (foto != null && foto.length > 0) {
                animal.setFotoAnimal(foto);
            }
            if (descricao != null){
                animal.setDescricao(descricao);
            }

            // Salva as mudanças no banco de dados
            animalRepository.save(animal);
            return Optional.of(animal);
        }

        // Retorna Optional.empty() se o animal não for encontrado
        return Optional.empty();
    }

    public List<AnimalModel> buscarAnimaisPorFiltros(TipoEspecie especie, String raca) {
        if (especie != null && !isValidRaca(especie, raca)) {
            throw new IllegalArgumentException("Raça inválida para a espécie fornecida.");
        }
        return animalRepository.findByFilters(especie, raca);
    }

    public List<AnimalModel> findAnimalsByIdCliente(Long idCliente) {
        return animalRepository.findByCliente_IdCliente(idCliente);
    }

    public Optional<AnimalModel> findByIdAnimal(Long idAnimal) {
        return animalRepository.findById(idAnimal);
    }

    //METODO PARA LISTAR TODAS OS ANIMAIS
    public List<AnimalModel> listarTodosAnimais() {
        return animalRepository.findAll();
    }
}



