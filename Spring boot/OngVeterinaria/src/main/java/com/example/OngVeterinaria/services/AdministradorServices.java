package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.PedidosTipo;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import com.example.OngVeterinaria.repository.*;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdministradorServices {
    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private ComprovanteRendaRepository comprovanteRendaRepository;

    @Autowired
    private DinheiroRepository dinheiroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private AnimalServices animalServices;

    public FuncionarioModel cadastrarFuncionario(FuncionarioModel funcionarioModel) {
        funcionarioModel.setPasswordFuncionario(passwordEncoder.encode(funcionarioModel.getPasswordFuncionario()));
        return funcionarioRepository.save(funcionarioModel);
    }


    public Optional<FuncionarioModel> deletarCliente(Long id_funcionario) {
        // Busca o animal pelo ID
        Optional<FuncionarioModel> cliente = funcionarioRepository.findById(id_funcionario);
        if (cliente.isPresent()) {
            // Se o animal existe, deleta-o
            funcionarioRepository.deleteById(id_funcionario);
        }
        return cliente; // Retorna o animal deletado, ou Optional.empty() se não foi encontrado
    }

    public boolean deletarFuncionario(Long idFuncionario) {
        // Busca o funcionário pelo ID
        Optional<FuncionarioModel> funcionario = funcionarioRepository.findById(idFuncionario);
        if (funcionario.isPresent()) {
            // Se o funcionário existe, deleta-o
            funcionarioRepository.deleteById(idFuncionario);
            return true;
        }
        return false; // Retorna false se o funcionário não foi encontrado
    }

    public boolean cancelarPedido(Long idPedido) {
        Optional<PedidoModel> pedido = pedidoRepository.findById(idPedido);
        if (pedido.isPresent()) {
            PedidoModel pedidoAtualizado = pedido.get();
            pedidoAtualizado.setStatusPedido(StatusGeral.CANCELADO); // Define o status para "CANCELADO"
            pedidoRepository.save(pedidoAtualizado); // Salva a alteração no banco
            return true;
        }
        return false; // Retorna false se o pedido não foi encontrado
    }

    public boolean aceitarPedido(Long id) {
        Optional<PedidoModel> pedidoOptional = pedidoRepository.findById(id);

        if (pedidoOptional.isPresent()) {
            PedidoModel pedido = pedidoOptional.get();
            pedido.setStatusPedido(StatusGeral.ANDAMENTO); // Define o status como "ANDAMENTO" do enum
            pedidoRepository.save(pedido);
            return true;
        }

        return false; // Retorna false se o pedido de doação não for encontrado
    }

    public Optional<FuncionarioModel> atualizarFuncionario(Long id, FuncionarioModel funcionarioAtualizado) {
        // Encontra o funcionário no banco de dados pelo id
        Optional<FuncionarioModel> funcionarioExistenteOptional = funcionarioRepository.findById(id);

        if (!funcionarioExistenteOptional.isPresent()) {
            return Optional.empty(); // Retorna vazio se o funcionário não for encontrado
        }

        FuncionarioModel funcionarioExistente = funcionarioExistenteOptional.get();

        // Verifica se o e-mail foi alterado (só se o novo e-mail for diferente do atual)
        if (funcionarioAtualizado.getEmail() != null && !funcionarioAtualizado.getEmail().equals(funcionarioExistente.getEmail())) {
            // Verifica se já existe outro funcionário com o mesmo e-mail
            FuncionarioModel funcionarioComEmail = funcionarioRepository.findByEmail(funcionarioAtualizado.getEmail());

            if (funcionarioComEmail != null) {
                // Se o e-mail já estiver em uso por outro funcionário, retorna erro
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já está em uso.");
            }
        }

        // Atualizando os campos necessários, se não forem nulos
        if (funcionarioAtualizado.getTipoFuncionario() != null) {
            funcionarioExistente.setTipoFuncionario(funcionarioAtualizado.getTipoFuncionario());
        }
        if (funcionarioAtualizado.getNome_funcionario() != null) {
            funcionarioExistente.setNome_funcionario(funcionarioAtualizado.getNome_funcionario());
        }

        // Só atualiza a senha se a nova senha não for nula nem vazia
        if (funcionarioAtualizado.getPasswordFuncionario() != null && !funcionarioAtualizado.getPasswordFuncionario().isEmpty()) {
            funcionarioExistente.setPasswordFuncionario(passwordEncoder.encode(funcionarioAtualizado.getPasswordFuncionario()));
        }

        if (funcionarioAtualizado.getCpf_funcionario() != null) {
            funcionarioExistente.setCpf_funcionario(funcionarioAtualizado.getCpf_funcionario());
        }

        // Atualiza o e-mail, se não for nulo
        if (funcionarioAtualizado.getEmail() != null) {
            funcionarioExistente.setEmail(funcionarioAtualizado.getEmail());
        }

        // Salvando as alterações
        return Optional.of(funcionarioRepository.save(funcionarioExistente));
    }



    public boolean emailJaExistente(String email) {
        // Verifica se já existe um funcionário com o mesmo e-mail
        return funcionarioRepository.findByEmail(email) != null; // Verifica se o retorno não é null
    }

    public DoacaoRelatorioDTO gerarRelatorioDoacoes(DoacaoRelatorioDTO relatorioDTO) throws DocumentException, IOException {
        if (relatorioDTO == null || relatorioDTO.getPedidos() == null || relatorioDTO.getPedidos().isEmpty()) {
            throw new IllegalArgumentException("Os dados do relatório estão vazios ou inválidos.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        // Abrindo o documento
        document.open();

        // Definindo as cores e fontes
        BaseColor primaryColor = new BaseColor(76, 175, 172);
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;
        BaseColor borderColor = new BaseColor(204, 204, 204);
        BaseColor rowColor = new BaseColor(240, 240, 240);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, whiteColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});

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
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título do relatório
        Paragraph titulo = new Paragraph("Relatório de Doações", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações de geração
        String nomeAdministrador = relatorioDTO.getFuncionario().getNome_funcionario();
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph infoGeracao = new Paragraph(String.format("Gerado por: %s\nData: %s", nomeAdministrador, dataAtual), smallFont);
        infoGeracao.setAlignment(Element.ALIGN_RIGHT);
        document.add(infoGeracao);

        document.add(new Paragraph("\n"));

        // Criando a tabela de pedidos
        PdfPTable tabela = new PdfPTable(5);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10f);
        tabela.setSpacingAfter(10f);
        tabela.setWidths(new float[]{3f, 3f, 2f, 2f, 2f});

        PdfPCell cell;
        cell = createHeaderCell("Cliente", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Animal", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Status", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Data", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("ID", primaryColor, borderColor);
        tabela.addCell(cell);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean isGray = false;

        for (PedidoModel pedido : relatorioDTO.getPedidos()) {
            BaseColor backgroundColor = isGray ? rowColor : whiteColor;

            tabela.addCell(createStyledCell(pedido.getCliente() != null ? pedido.getCliente().getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(pedido.getAnimal() != null ? pedido.getAnimal().getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(pedido.getStatusPedido() != null ? pedido.getStatusPedido().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(pedido.getDataPedido() != null ? pedido.getDataPedido().format(formatter) : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(pedido.getIdPedido()), backgroundColor));

            isGray = !isGray;
        }

        document.add(tabela);

        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        document.close();

        // Atualiza o DTO com o PDF gerado
        relatorioDTO.setRelatorio(byteArrayOutputStream.toByteArray());

        return relatorioDTO;
    }

    public AdocaoRelatorioDTO gerarRelatorioAdocoes(AdocaoRelatorioDTO relatorioDTO) throws DocumentException, IOException {
        if (relatorioDTO == null || relatorioDTO.getPedidos() == null || relatorioDTO.getPedidos().isEmpty()) {
            throw new IllegalArgumentException("Os dados do relatório estão vazios ou inválidos.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        // Abrindo o documento
        document.open();

        // Definindo as cores e fontes
        BaseColor primaryColor = new BaseColor(76, 175, 172);
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;
        BaseColor borderColor = new BaseColor(204, 204, 204);
        BaseColor rowColor = new BaseColor(240, 240, 240);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, whiteColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});

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
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título do relatório
        Paragraph titulo = new Paragraph("Relatório de Adoções", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações de geração
        String nomeAdministrador = relatorioDTO.getFuncionario().getNome_funcionario();
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph infoGeracao = new Paragraph(String.format("Gerado por: %s\nData: %s", nomeAdministrador, dataAtual), smallFont);
        infoGeracao.setAlignment(Element.ALIGN_RIGHT);
        document.add(infoGeracao);

        document.add(new Paragraph("\n"));

        // Criando a tabela de pedidos
        PdfPTable tabela = new PdfPTable(5);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10f);
        tabela.setSpacingAfter(10f);
        tabela.setWidths(new float[]{3f, 3f, 2f, 2f, 2f});

        PdfPCell cell;
        cell = createHeaderCell("Cliente", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Animal", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Status", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Data", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("ID", primaryColor, borderColor);
        tabela.addCell(cell);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        boolean isGray = false;

        for (PedidoModel pedido : relatorioDTO.getPedidos()) {
            BaseColor backgroundColor = isGray ? rowColor : whiteColor;

            tabela.addCell(createStyledCell(pedido.getCliente() != null ? pedido.getCliente().getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(pedido.getAnimal() != null ? pedido.getAnimal().getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(pedido.getStatusPedido() != null ? pedido.getStatusPedido().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(pedido.getDataPedido() != null ? pedido.getDataPedido().format(formatter) : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(pedido.getIdPedido()), backgroundColor));

            isGray = !isGray;
        }

        document.add(tabela);

        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        document.close();

        // Atualiza o DTO com o PDF gerado
        relatorioDTO.setRelatorio(byteArrayOutputStream.toByteArray());

        return relatorioDTO;
    }

    public DenunciaRelatorioDTO gerarRelatorioDenuncias(DenunciaRelatorioDTO relatorioDTO) throws DocumentException, IOException {
        if (relatorioDTO == null || relatorioDTO.getDenuncias() == null || relatorioDTO.getDenuncias().isEmpty()) {
            throw new IllegalArgumentException("Os dados do relatório estão vazios ou inválidos.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        // Abrindo o documento
        document.open();

        // Definindo as cores e fontes
        BaseColor primaryColor = new BaseColor(76, 175, 172);
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;
        BaseColor borderColor = new BaseColor(204, 204, 204);
        BaseColor rowColor = new BaseColor(240, 240, 240);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, whiteColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});

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
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título do relatório
        Paragraph titulo = new Paragraph("Relatório de Denúncias", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações de geração
        String nomeAdministrador = relatorioDTO.getFuncionario().getNome_funcionario();
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph infoGeracao = new Paragraph(String.format("Gerado por: %s\nData: %s", nomeAdministrador, dataAtual), smallFont);
        infoGeracao.setAlignment(Element.ALIGN_RIGHT);
        document.add(infoGeracao);

        document.add(new Paragraph("\n"));

        // Criando a tabela de denúncias
        PdfPTable tabela = new PdfPTable(4);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10f);
        tabela.setSpacingAfter(10f);
        tabela.setWidths(new float[]{3f, 3f, 2f, 2f});

        PdfPCell cell;
        cell = createHeaderCell("Cliente", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Tipo da Denúncia", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Status", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("ID", primaryColor, borderColor);
        tabela.addCell(cell);

        boolean isGray = false;

        for (DenunciaModel denuncia : relatorioDTO.getDenuncias()) {
            BaseColor backgroundColor = isGray ? rowColor : whiteColor;

            tabela.addCell(createStyledCell(denuncia.getCliente() != null ? denuncia.getCliente().getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(denuncia.getTipoDenucias() != null ? denuncia.getTipoDenucias().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(denuncia.getStatusGeral() != null ? denuncia.getStatusGeral().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(denuncia.getIdDenuncia()), backgroundColor));

            isGray = !isGray;
        }

        document.add(tabela);

        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        document.close();

        // Atualiza o DTO com o PDF gerado
        relatorioDTO.setRelatorio(byteArrayOutputStream.toByteArray());

        return relatorioDTO;
    }

    public AnimalRelatorioDTO gerarRelatorioAnimais(AnimalRelatorioDTO relatorioDTO) throws DocumentException, IOException {
        if (relatorioDTO == null || relatorioDTO.getAnimais() == null || relatorioDTO.getAnimais().isEmpty()) {
            throw new IllegalArgumentException("Os dados do relatório estão vazios ou inválidos.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        // Abrindo o documento
        document.open();

        // Definindo as cores e fontes
        BaseColor primaryColor = new BaseColor(76, 175, 172);
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;
        BaseColor borderColor = new BaseColor(204, 204, 204);
        BaseColor rowColor = new BaseColor(240, 240, 240);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, whiteColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});

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
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título do relatório
        Paragraph titulo = new Paragraph("Relatório de Animais", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações de geração
        String nomeAdministrador = relatorioDTO.getFuncionario().getNome_funcionario();
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph infoGeracao = new Paragraph(String.format("Gerado por: %s\nData: %s", nomeAdministrador, dataAtual), smallFont);
        infoGeracao.setAlignment(Element.ALIGN_RIGHT);
        document.add(infoGeracao);

        document.add(new Paragraph("\n"));

        // Criando a tabela de animais
        PdfPTable tabela = new PdfPTable(9);
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10f);
        tabela.setSpacingAfter(10f);
        tabela.setWidths(new float[]{3f, 3f, 2f, 2f, 2f, 2f, 2f, 2f, 2f});

        PdfPCell cell;
        cell = createHeaderCell("Cliente", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Nome do Animal", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Espécie", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Raça", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Sexo", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Idade", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Cor", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Peso", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("ID", primaryColor, borderColor);
        tabela.addCell(cell);

        boolean isGray = false;

        for (AnimalModel animal : relatorioDTO.getAnimais()) {
            BaseColor backgroundColor = isGray ? rowColor : whiteColor;

            tabela.addCell(createStyledCell(animal.getCliente() != null ? animal.getCliente().getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(animal.getNome() != null ? animal.getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(animal.getEspecie() != null ? animal.getEspecie().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(animal.getRaca() != null ? animal.getRaca() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(animal.getSexo() != null ? animal.getSexo().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(animal.getIdade()), backgroundColor));
            tabela.addCell(createStyledCell(animal.getCor() != null ? animal.getCor() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(animal.getPeso()), backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(animal.getIdAnimal()), backgroundColor));

            isGray = !isGray;
        }

        document.add(tabela);

        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        document.close();

        // Atualiza o DTO com o PDF gerado
        relatorioDTO.setRelatorio(byteArrayOutputStream.toByteArray());

        return relatorioDTO;
    }

    public ClienteRelatorioDTO gerarRelatorioClientes(ClienteRelatorioDTO relatorioDTO) throws DocumentException, IOException {
        if (relatorioDTO == null || relatorioDTO.getClientes() == null || relatorioDTO.getClientes().isEmpty()) {
            throw new IllegalArgumentException("Os dados do relatório estão vazios ou inválidos.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        // Abrindo o documento
        document.open();

        // Definindo as cores e fontes
        BaseColor primaryColor = new BaseColor(76, 175, 172);
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;
        BaseColor borderColor = new BaseColor(204, 204, 204);
        BaseColor rowColor = new BaseColor(240, 240, 240);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, whiteColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});

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
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título do relatório
        Paragraph titulo = new Paragraph("Relatório de Clientes", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações de geração
        String nomeAdministrador = relatorioDTO.getFuncionario().getNome_funcionario();
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph infoGeracao = new Paragraph(String.format("Gerado por: %s\nData: %s", nomeAdministrador, dataAtual), smallFont);
        infoGeracao.setAlignment(Element.ALIGN_RIGHT);
        document.add(infoGeracao);

        document.add(new Paragraph("\n"));

        // Criando a tabela de clientes
        PdfPTable tabela = new PdfPTable(8); // Agora com 8 colunas, incluindo a data de cadastro
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10f);
        tabela.setSpacingAfter(10f);
        tabela.setWidths(new float[]{7f, 7f, 6.5f, 7.5f, 8f, 6f, 5.5f, 2f}); // Ajuste conforme necessário

        PdfPCell cell;
        cell = createHeaderCell("Nome", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Gênero", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Nascimento", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("CPF", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Telefone", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("E-mail", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Data Cadastro", primaryColor, borderColor); // Adicionando a Data Cadastro
        tabela.addCell(cell);
        cell = createHeaderCell("ID", primaryColor, borderColor);
        tabela.addCell(cell);

        boolean isGray = false;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (ClienteModel cliente : relatorioDTO.getClientes()) {
            BaseColor backgroundColor = isGray ? rowColor : whiteColor;

            tabela.addCell(createStyledCell(cliente.getNome() != null ? cliente.getNome() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(cliente.getGeneroCliente() != null ? cliente.getGeneroCliente().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(cliente.getData_nascimento() != null ? cliente.getData_nascimento().format(formatter) : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(cliente.getCpf() != null ? cliente.getCpf() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(cliente.getTelefone() != null ? cliente.getTelefone() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(cliente.getEmail() != null ? cliente.getEmail() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(cliente.getData_Cadastro() != null ? cliente.getData_Cadastro().format(formatter) : "Não informado", backgroundColor)); // Data Cadastro
            tabela.addCell(createStyledCell(String.valueOf(cliente.getIdCliente()), backgroundColor));

            isGray = !isGray;  // Alterna a cor de fundo
        }

        // Adiciona a tabela no documento
        document.add(tabela);

        // Fecha o documento e o escritor
        document.close();

        // Atualiza o DTO com o PDF gerado
        relatorioDTO.setRelatorio(byteArrayOutputStream.toByteArray());

        return relatorioDTO;
    }

    public FuncionarioRelatorioDTO gerarRelatorioFuncionarios(FuncionarioRelatorioDTO relatorioDTO) throws DocumentException, IOException {
        if (relatorioDTO == null || relatorioDTO.getFuncionarios() == null || relatorioDTO.getFuncionarios().isEmpty()) {
            throw new IllegalArgumentException("Os dados do relatório estão vazios ou inválidos.");
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        // Abrindo o documento
        document.open();

        // Definindo as cores e fontes
        BaseColor primaryColor = new BaseColor(76, 175, 172);
        BaseColor blackColor = BaseColor.BLACK;
        BaseColor whiteColor = BaseColor.WHITE;
        BaseColor borderColor = new BaseColor(204, 204, 204);
        BaseColor rowColor = new BaseColor(240, 240, 240);

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, blackColor);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, whiteColor);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, blackColor);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, blackColor);

        // Cabeçalho
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{1, 3});

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
        document.add(new Chunk(new LineSeparator(1f, 100f, primaryColor, Element.ALIGN_CENTER, -2)));

        // Título do relatório
        Paragraph titulo = new Paragraph("Relatório de Funcionários", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(20);
        document.add(titulo);

        // Informações de geração
        String nomeAdministrador = relatorioDTO.getAdministrador().getNome_funcionario();
        String dataAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph infoGeracao = new Paragraph(String.format("Gerado por: %s\nData: %s", nomeAdministrador, dataAtual), smallFont);
        infoGeracao.setAlignment(Element.ALIGN_RIGHT);
        document.add(infoGeracao);

        document.add(new Paragraph("\n"));

        // Criando a tabela de funcionários
        PdfPTable tabela = new PdfPTable(6); // Seis colunas
        tabela.setWidthPercentage(100);
        tabela.setSpacingBefore(10f);
        tabela.setSpacingAfter(10f);
        tabela.setWidths(new float[]{4f, 3.5f, 4, 4.5f, 3f, 1f}); // Ajuste conforme necessário

        PdfPCell cell;
        cell = createHeaderCell("Nome", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("CPF", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("E-mail", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Tipo de Funcionário", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("Data Cadastro", primaryColor, borderColor);
        tabela.addCell(cell);
        cell = createHeaderCell("ID", primaryColor, borderColor);
        tabela.addCell(cell);

        boolean isGray = false;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (FuncionarioModel funcionario : relatorioDTO.getFuncionarios()) {
            BaseColor backgroundColor = isGray ? rowColor : whiteColor;

            tabela.addCell(createStyledCell(funcionario.getNome_funcionario() != null ? funcionario.getNome_funcionario() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(funcionario.getCpf_funcionario() != null ? funcionario.getCpf_funcionario() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(funcionario.getEmail() != null ? funcionario.getEmail() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(funcionario.getTipoFuncionario() != null ? funcionario.getTipoFuncionario().toString() : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(funcionario.getData_emissao() != null ? funcionario.getData_emissao().format(formatter) : "Não informado", backgroundColor));
            tabela.addCell(createStyledCell(String.valueOf(funcionario.getId_funcionario()), backgroundColor));

            isGray = !isGray;  // Alterna a cor de fundo
        }

        // Adiciona a tabela no documento
        document.add(tabela);

        // Fecha o documento e o escritor
        document.close();

        // Atualiza o DTO com o PDF gerado
        relatorioDTO.setRelatorio(byteArrayOutputStream.toByteArray());

        return relatorioDTO;
    }

    private PdfPCell createHeaderCell(String text, BaseColor backgroundColor, BaseColor borderColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE)));
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createStyledCell(String text, BaseColor backgroundColor) {
        BaseColor borderColor = new BaseColor(204, 204, 204); // Cinza claro

        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 12)));
        cell.setBackgroundColor(backgroundColor);
        cell.setBorderColor(borderColor);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private <T> List<T> preencherComZero(Map<String, T> dados, List<String> mesesOrdenados, T valorDefault) {
        return mesesOrdenados.stream()
                .map(m -> dados.getOrDefault(m, valorDefault))
                .collect(Collectors.toList());
    }

    private String formatMes(String mes) {
        if (mes == null || mes.isEmpty()) {
            return ""; // Retorna vazio se o mês for nulo ou vazio
        }

        // Mapeamento de meses em inglês para português
        Map<String, String> mesMap = new HashMap<>();
        mesMap.put("January", "Janeiro");
        mesMap.put("February", "Fevereiro");
        mesMap.put("March", "Março");
        mesMap.put("April", "Abril");
        mesMap.put("May", "Maio");
        mesMap.put("June", "Junho");
        mesMap.put("July", "Julho");
        mesMap.put("August", "Agosto");
        mesMap.put("September", "Setembro");
        mesMap.put("October", "Outubro");
        mesMap.put("November", "Novembro");
        mesMap.put("December", "Dezembro");

        // Garante que o mês seja tratado como case-insensitive (ex.: "APRIL" -> "April")
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1).toLowerCase();

        // Retorna o mês traduzido, ou o original se não for encontrado
        return mesMap.getOrDefault(mes, mes);
    }

    public BufferedImage gerarGraficoAdocaoDoacao() {
        // Filtrar serviços por tipo e status CONCLUÍDO
        List<PedidoModel> adocoes = pedidoRepository.findByTipoAndStatusPedido(PedidosTipo.ADOCAO, StatusGeral.CONCLUIDO);
        List<PedidoModel> doacoes = pedidoRepository.findByTipoAndStatusPedido(PedidosTipo.DOACAO, StatusGeral.CONCLUIDO);

        // Organizar dados por mês
        Map<String, Long> dadosAdocoes = adocoes.stream()
                .collect(Collectors.groupingBy(p -> formatMes(p.getMes()), Collectors.counting()));

        Map<String, Long> dadosDoacoes = doacoes.stream()
                .collect(Collectors.groupingBy(p -> formatMes(p.getMes()), Collectors.counting()));

        // Ordem fixa dos meses
        List<String> mesesOrdenados = Arrays.asList(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        );

        // Preencher valores com 0 para os meses ausentes
        List<Long> valoresAdocoes = mesesOrdenados.stream()
                .map(m -> dadosAdocoes.getOrDefault(m, 0L))
                .collect(Collectors.toList());

        List<Long> valoresDoacoes = mesesOrdenados.stream()
                .map(m -> dadosDoacoes.getOrDefault(m, 0L))
                .collect(Collectors.toList());

        // Criar o gráfico
        CategoryChart chart = new CategoryChartBuilder()
                .width(1000)
                .height(290)
                .theme(Styler.ChartTheme.Matlab)
                .build();

        // Adicionar séries com valores (mesmo que 0)
        chart.addSeries("Adoções", mesesOrdenados, valoresAdocoes);
        chart.addSeries("Doações", mesesOrdenados, valoresDoacoes);

        // Configurações do gráfico
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.decode("#f9f9f9"));
        chart.getStyler().setPlotGridLinesColor(Color.BLACK);
        chart.getStyler().setAxisTickLabelsColor(Color.BLACK);
        chart.getStyler().setXAxisTitleColor(Color.BLACK);
        chart.getStyler().setYAxisTitleColor(Color.BLACK);
        chart.getStyler().setLegendBorderColor(Color.WHITE);
        chart.getStyler().setLegendBackgroundColor(Color.WHITE);
        chart.getStyler().setSeriesColors(new Color[]{
                Color.decode("#15999b"), // Adoções
                Color.decode("#1b4154")  // Doações
        });

        chart.getStyler().setLegendVisible(true); // Legenda visível
        chart.getStyler().setChartTitleVisible(false); // Título invisível
        chart.getStyler().setAxisTitlesVisible(false); // Títulos dos eixos invisíveis
        chart.getStyler().setDecimalPattern("#"); // Números inteiros

        // Verificar se ambos os gráficos estão vazios e, se sim, exibir gráfico vazio
        if (valoresAdocoes.stream().allMatch(v -> v == 0) && valoresDoacoes.stream().allMatch(v -> v == 0)) {
            // Exibir gráfico sem registros, mas com a estrutura
            chart.addSeries("Adoções", mesesOrdenados, valoresAdocoes); // Série vazia
            chart.addSeries("Doações", mesesOrdenados, valoresDoacoes); // Série vazia
        }

        return BitmapEncoder.getBufferedImage(chart);
    }

    public BufferedImage gerarGraficoDenuncia() {
        // Consultar o banco de dados para obter as denúncias concluídas por tipo
        long violencia = denunciaRepository.countByTipoDenuciasAndStatusGeral(TipoDenucias.VIOLENCIA, StatusGeral.CONCLUIDO);
        long perdido = denunciaRepository.countByTipoDenuciasAndStatusGeral(TipoDenucias.PERDIDO, StatusGeral.CONCLUIDO);
        long abandonado = denunciaRepository.countByTipoDenuciasAndStatusGeral(TipoDenucias.ABANDONADO, StatusGeral.CONCLUIDO);

        // Criar gráfico de barras
        CategoryChart chart = new CategoryChartBuilder()
                .width(320)
                .height(250)
                .theme(Styler.ChartTheme.Matlab)
                .build();

        // Adicionar séries mesmo que com 0
        chart.addSeries("Violência", Collections.singletonList(""), Collections.singletonList(violencia));
        chart.addSeries("Perdido", Collections.singletonList(""), Collections.singletonList(perdido));
        chart.addSeries("Abandonado", Collections.singletonList(""), Collections.singletonList(abandonado));

        // Configurações de estilo
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.decode("#f9f9f9"));
        chart.getStyler().setPlotGridLinesColor(Color.BLACK);
        chart.getStyler().setAxisTickLabelsColor(Color.BLACK);
        chart.getStyler().setXAxisTitleColor(Color.BLACK);
        chart.getStyler().setYAxisTitleColor(Color.BLACK);
        chart.getStyler().setLegendBorderColor(Color.WHITE);
        chart.getStyler().setLegendBackgroundColor(Color.WHITE);

        // Ajustes de cores
        chart.getStyler().setSeriesColors(new Color[]{
                Color.decode("#1b4154"), // Violência
                Color.decode("#15999b"), // Perdido
                Color.decode("#e8fefc")  // Abandonado
        });

        // Configurações adicionais
        chart.getStyler().setChartTitleVisible(false); // Remover título
        chart.getStyler().setLegendVisible(true); // Exibir legenda
        chart.getStyler().setAxisTitlesVisible(false);
        chart.getStyler().setDecimalPattern("#"); // Inteiros

        // Remover rótulos do eixo X
        chart.getStyler().setXAxisTicksVisible(false);

        // Gerar e retornar a imagem do gráfico
        return BitmapEncoder.getBufferedImage(chart);
    }


    public BufferedImage gerarGraficoDoacoesDinheiro() {
        // Buscar todas as doações em dinheiro
        List<DinheiroModel> dinheiro = dinheiroRepository.findAll();

        // Verificar se existem doações
        if (dinheiro.isEmpty()) {
            return gerarGraficoVazio("Sem Dados de Doações em Dinheiro");
        }

        // Mapear os meses das doações em dinheiro para os valores
        Map<String, Double> dadosDinheiro = dinheiro.stream()
                .collect(Collectors.groupingBy(
                        d -> formatMes(d.getDataDoacao().getMonth().name()), // Obter o nome do mês (em inglês)
                        Collectors.summingDouble(DinheiroModel::getValor)
                ));

        // Ordem fixa dos meses em português
        List<String> mesesOrdenados = Arrays.asList(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        );

        // Preparar os dados para o gráfico com preenchimento de 0 para meses ausentes
        List<Double> valoresDinheiro = mesesOrdenados.stream()
                .map(m -> dadosDinheiro.getOrDefault(m, 0.0)) // Usando o método formatado para os meses
                .collect(Collectors.toList());

        // Criar gráfico de linhas
        XYChart chart = new XYChartBuilder()
                .width(660)
                .height(250)
                .theme(Styler.ChartTheme.Matlab)
                .build();

        // Adicionar série de dados de doações em dinheiro (valores)
        chart.addSeries("Doações em Dinheiro",
                mesesOrdenados.stream().mapToDouble(m -> mesesOrdenados.indexOf(m) + 1).toArray(),
                valoresDinheiro.stream().mapToDouble(Double::doubleValue).toArray());

        // Configuração do eixo Y para exibir "R$" antes dos valores
        chart.setCustomYAxisTickLabelsFormatter(value -> "R$ " + String.format("%.2f", value));

        // Configurar o título do eixo X
        chart.setXAxisTitle("Meses");

        // Estilizar o gráfico
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotBackgroundColor(Color.decode("#f9f9f9"));
        chart.getStyler().setPlotGridLinesColor(Color.BLACK);
        chart.getStyler().setAxisTickLabelsColor(Color.BLACK);
        chart.getStyler().setXAxisTitleColor(Color.BLACK);
        chart.getStyler().setYAxisTitleColor(Color.BLACK);
        chart.getStyler().setLegendBorderColor(Color.WHITE);
        chart.getStyler().setLegendBackgroundColor(Color.WHITE);

        chart.getStyler().setSeriesColors(new Color[]{Color.decode("#14999a")});

        chart.getStyler().setChartTitleVisible(false); // Título invisível
        chart.getStyler().setLegendVisible(true); // Exibir legenda
        chart.getStyler().setXAxisTitleVisible(true); // Exibir título do eixo X
        chart.getStyler().setYAxisTitleVisible(false);

        // Gerar e retornar a imagem do gráfico
        return BitmapEncoder.getBufferedImage(chart);
    }


    private BufferedImage gerarGraficoVazio(String mensagem) {
        // Implementação do gráfico vazio com uma mensagem
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); // Placeholder vazio
    }
}
