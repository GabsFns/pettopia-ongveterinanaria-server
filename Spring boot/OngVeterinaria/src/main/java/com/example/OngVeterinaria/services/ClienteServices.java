package com.example.OngVeterinaria.services;

import com.example.OngVeterinaria.DTO.ClienteDTO;
import com.example.OngVeterinaria.DTO.DenunciaDTO;
import com.example.OngVeterinaria.DTO.EnderecoDTO;
import com.example.OngVeterinaria.model.*;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import com.example.OngVeterinaria.repository.ClienteRepository;
import com.example.OngVeterinaria.repository.DenunciaRepository;
import com.example.OngVeterinaria.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


@Service
public class ClienteServices {
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DenunciaRepository denunciaRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;


    // Método para cadastrar cliente
    public ClienteModel cadastrarCliente(ClienteModel cliente) {
        if (clienteRepository.existsByEmail(cliente.getEmail())){
            throw new RuntimeException("Email tem que ser Unico");
        }
        if (clienteRepository.existsByCpf(cliente.getCpf())){
            throw new RuntimeException("Cpf tem que ser unico");
        }
        if (clienteRepository.existsByTelefone(cliente.getTelefone())){
            throw new RuntimeException("Telefone tem que ser unico");
        }
        if (clienteRepository.existsByEmail(cliente.getEmail()) && !(clienteRepository.existsByCpf(cliente.getCpf()))){
            throw new RuntimeException("Usuario existente, add um email");
        }

        // Criptografando a senha do cliente
        cliente.setPassword_Cliente(passwordEncoder.encode(cliente.getPassword_Cliente()));
        // Salvando o cliente
        return clienteRepository.save(cliente);
    }

    public ClienteModel cadastrarClienteJava(ClienteModel cliente) {
        if (clienteRepository.existsByEmail(cliente.getEmail())) {
            throw new RuntimeException("Email tem que ser único");
        }
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new RuntimeException("CPF tem que ser único");
        }
        if (clienteRepository.existsByTelefone(cliente.getTelefone())) {
            throw new RuntimeException("Telefone tem que ser único");
        }

        // Criptografando a senha somente se não for nula
        if (cliente.getPassword_Cliente() != null) {
            cliente.setPassword_Cliente(passwordEncoder.encode(cliente.getPassword_Cliente()));
        }

        // Salvando o cliente
        return clienteRepository.save(cliente);
    }

    // Método de login do cliente
    public Optional<ClienteModel> login(String email, String password_Cliente) {
        // Buscando o cliente pelo email
        Optional<ClienteModel> clienteOpt = clienteRepository.findByEmail(email);
        if (clienteOpt == null){
            throw new RuntimeException("Credenciais inválidas");
        }
        if (clienteOpt.isPresent() && passwordEncoder.matches(password_Cliente, clienteOpt.get().getPassword_Cliente())) {
            return clienteOpt;
        }
        return Optional.empty();
    }
    //Check de senha web
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public ClienteModel findByEmail(String email) {
        return clienteRepository.findByEmail(email).orElse(null);
    }

    public List<ClienteModel> listarCliente(){
        return clienteRepository.findAll();
    }

    // Buscar cliente por ID
    public Optional<ClienteModel> buscarClientePorId(Long id) {
        return clienteRepository.findById(id);
    }

    // Atualizar dados do cliente
    public Optional<ClienteModel> atualizarCliente(Long id, ClienteModel clienteAtualizado) {
        return clienteRepository.findById(id).map(clienteExistente -> {
            // Atualizando os campos necessários se não forem nulos
            if (clienteAtualizado.getCpf() != null) {
                clienteExistente.setCpf(clienteAtualizado.getCpf());
            }
            if (clienteAtualizado.getNome() != null) {
                clienteExistente.setNome(clienteAtualizado.getNome());
            }
            if (clienteAtualizado.getEmail() != null) {
                clienteExistente.setEmail(clienteAtualizado.getEmail());
            }
            if (clienteAtualizado.getPassword_Cliente() != null) {
                clienteExistente.setPassword_Cliente(passwordEncoder.encode(clienteAtualizado.getPassword_Cliente()));
            }
            if (clienteAtualizado.getTelefone() != null) {
                clienteExistente.setTelefone(clienteAtualizado.getTelefone());
            }
            if (clienteAtualizado.getGeneroCliente() != null) {
                clienteExistente.setGeneroCliente(clienteAtualizado.getGeneroCliente());
            }
            if (clienteAtualizado.getData_nascimento() != null) {
                clienteExistente.setData_nascimento(clienteAtualizado.getData_nascimento());
            }

            // Salvando as alterações
            return clienteRepository.save(clienteExistente);
        });
    }

    // Gerar token e envio de e-mail
    private static final int TOKEN_LENGTH = 6; // Comprimento do token
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();

    // Método para gerar token de recuperação de senha
    public void gerarTokenRecuperacao(String email) {
        Optional<ClienteModel> clienteOpt = clienteRepository.findByEmail(email);
        clienteOpt.ifPresentOrElse(cliente -> {
            String token = gerarTokenAleatorio();
            cliente.setResetToken(token);
            cliente.setResetTokenExpiration(LocalDateTime.now().plusHours(1)); // Expiração em 1 hora
            clienteRepository.save(cliente);

            enviarEmailRecuperacao(email, token);
        }, () -> {
            throw new RuntimeException("Cliente não encontrado.");
        });
    }

    // Gerador de token aleatório
    private String gerarTokenAleatorio() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            token.append(CHARACTERS.charAt(index));
        }
        return token.toString();
    }

    // Método para enviar e-mail de recuperação de senha
    private void enviarEmailRecuperacao(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom("no-reply@suaempresa.com");
            message.setSubject("Redefinição de Senha");
            message.setText("Olá, utilize o seguinte código para redefinir sua senha: " + token);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar o e-mail.", e);
        }
    }

    // Método para validar o token de recuperação
    public boolean validarToken(String email, String token) {
        return clienteRepository.findByEmail(email)
                .map(cliente -> token.equals(cliente.getResetToken()) &&
                        LocalDateTime.now().isBefore(cliente.getResetTokenExpiration()))
                .orElse(false);
    }

    // Método para atualizar senha
    public void atualizarSenha(String email, String novaSenha) {
        Optional<ClienteModel> clienteOpt = clienteRepository.findByEmail(email);
        clienteOpt.ifPresentOrElse(cliente -> {
            cliente.setPassword_Cliente(passwordEncoder.encode(novaSenha)); // Aplica hash
            cliente.setResetToken(null); // Limpa o token
            cliente.setResetTokenExpiration(null); // Limpa expiração
            clienteRepository.save(cliente);
        }, () -> {
            throw new RuntimeException("Cliente não encontrado.");
        });
    }

    // Métodos para denúncias
    public DenunciaModel realizarDenuncia(DenunciaModel denunciaModel) {
        try {
            // Obtém o cliente pelo ID
            ClienteModel cliente = clienteRepository.findById(denunciaModel.getCliente().getIdCliente())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

            EnderecoModel enderecoModel = denunciaModel.getEndereco();

            // Verifica se o endereço está presente
            if (enderecoModel != null) {
                // Verifica se o endereço já existe
                EnderecoModel enderecoExistente = enderecoRepository.findByLogradouroAndNumeroAndBairro(
                        enderecoModel.getLogradouro(),
                        enderecoModel.getNumero(),
                        enderecoModel.getBairro()
                );

                if (enderecoExistente != null) {
                    // Se o endereço já existe, use-o
                    denunciaModel.setEndereco(enderecoExistente);
                } else {
                    // Se não existe, salve o novo endereço sem associar cliente
                    EnderecoModel enderecoSalvo = enderecoRepository.save(enderecoModel);
                    denunciaModel.setEndereco(enderecoSalvo);
                }
            } else {
                throw new RuntimeException("Endereço não pode ser nulo.");
            }

            // Associa o cliente à denúncia
            denunciaModel.setCliente(cliente);

            // Salva a denúncia
            return denunciaRepository.save(denunciaModel);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar a denúncia: " + e.getMessage());
        }
    }

    public List<Object[]> getCountDenunciasByTipoAndCliente(Long idCliente) {
        return denunciaRepository.countDenunciasByTipo(idCliente);
    }

    public DenunciaModel atualizarDenuncia(Long idDenuncia, DenunciaModel denunciaAtualizada) {
        // Verifica se a denúncia existe
        DenunciaModel denunciaExistente = denunciaRepository.findById(idDenuncia)
                .orElseThrow(() -> new RuntimeException("Denúncia não encontrada"));

        // Verifica se o endereço associado à denúncia existe
        EnderecoModel enderecoExistente = denunciaExistente.getEndereco(); // Assume que a denúncia tem um método getEndereco()

        if (enderecoExistente == null) {
            throw new RuntimeException("Endereço referente à denúncia não encontrado");
        }

        // Atualiza os campos do endereço existente
        if (denunciaAtualizada.getEndereco() != null) {
            // Verifica se há novos valores para complemento e número
            if (denunciaAtualizada.getEndereco().getComplemento() != null) {
                enderecoExistente.setComplemento(denunciaAtualizada.getEndereco().getComplemento());
            }
            if (denunciaAtualizada.getEndereco().getNumero() != null) {
                enderecoExistente.setNumero(denunciaAtualizada.getEndereco().getNumero());
            }
        }

        // Atualiza os campos da denúncia existente
        if (denunciaAtualizada.getDescricao() != null) {
            denunciaExistente.setDescricao(denunciaAtualizada.getDescricao());
        }
        if (denunciaAtualizada.getStatusGeral() != null) {
            denunciaExistente.setStatusGeral(denunciaAtualizada.getStatusGeral());
        }
        if (denunciaAtualizada.getTipoDenucias() != null) {
            denunciaExistente.setTipoDenucias(denunciaAtualizada.getTipoDenucias());
        }

        // Salva o endereço atualizado
        enderecoRepository.save(enderecoExistente); // Atualiza o endereço

        // Salva a denúncia atualizada no banco de dados
        return denunciaRepository.save(denunciaExistente); // Atualiza a denúncia
    }


    public List<DenunciaModel> listarTodasDenuncias() {
        return denunciaRepository.findAll();
    }

//    public List<DenunciaModel> buscarPorCliente(Long idCliente) {
//        return denunciaRepository.findByCliente_IdCliente(idCliente);
//    }

    public List<DenunciaModel> buscarPorData(LocalDate dataDenuncia) {
        return denunciaRepository.findByDataDenuncia(dataDenuncia);
    }

    public List<DenunciaModel> buscarPorTipo(TipoDenucias tipoDenucias) {
        return denunciaRepository.findByTipoDenucias(tipoDenucias);
    }

    public List<DenunciaModel> buscarPorClienteDataETipo(Long idCliente, LocalDate dataDenuncia, TipoDenucias tipoDenucias) {
        return denunciaRepository.buscarPorClienteDataETipo(idCliente, dataDenuncia, tipoDenucias);
    }

    public boolean deletarCliente(Long idCliente) {
        // Busca o cliente pelo ID
        Optional<ClienteModel> cliente = clienteRepository.findById(idCliente);
        if (cliente.isPresent()) {
            // Se o cliente existe, deleta-o
            clienteRepository.deleteById(idCliente);
            return true;
        }
        return false; // Retorna false se o funcionário não foi encontrado
    }

    public ClienteDTO convertToDTO(ClienteModel cliente) {
        ClienteDTO clienteDTO = new ClienteDTO();
        clienteDTO.setIdCliente(cliente.getIdCliente());
        clienteDTO.setCpf(cliente.getCpf());
        clienteDTO.setNome(cliente.getNome());
        clienteDTO.setEmail(cliente.getEmail());
        clienteDTO.setTelefone(cliente.getTelefone());
        clienteDTO.setGenero(cliente.getGeneroCliente());
        clienteDTO.setDataNascimento(cliente.getData_nascimento());

        // Buscar as denúncias do cliente e convertê-las para DenunciaDTO


        return clienteDTO;
    }


    public List<DenunciaDTO> buscarPorCliente(Long idCliente) {
        List<DenunciaModel> denuncias = denunciaRepository.findByCliente_IdCliente(idCliente);

        // Convertendo as denúncias para DTOs
        return denuncias.stream()
                .map(denuncia -> {
                    // Convertendo EnderecoModel para EnderecoDTO
                    EnderecoDTO enderecoDTO = new EnderecoDTO();
                    enderecoDTO.setIdEndereco(denuncia.getEndereco().getIdEndereco());
                    enderecoDTO.setLogradouro(denuncia.getEndereco().getLogradouro());
                    enderecoDTO.setNumero(denuncia.getEndereco().getNumero());
                    enderecoDTO.setBairro(denuncia.getEndereco().getBairro());
                    enderecoDTO.setComplemento(denuncia.getEndereco().getComplemento());
                    enderecoDTO.setUf(denuncia.getEndereco().getUf());
                    enderecoDTO.setCep(denuncia.getEndereco().getCep());

                    // Convertendo DenunciaModel para DenunciaDTO
                    DenunciaDTO denunciaDTO = new DenunciaDTO();
                    denunciaDTO.setIdDenuncia(denuncia.getIdDenuncia());
                    denunciaDTO.setDescricao(denuncia.getDescricao());
                    denunciaDTO.setDataDenuncia(denuncia.getDataDenuncia());
                    denunciaDTO.setTipoDenuncia(denuncia.getTipoDenucias());
                    denunciaDTO.setStatusGeral(denuncia.getStatusGeral());  // Inclui o status
                    denunciaDTO.setEnderecoDenuncia(enderecoDTO);

                    return denunciaDTO;
                })
                .collect(Collectors.toList());
    }


}
