package com.example.OngVeterinaria.repository;


import com.example.OngVeterinaria.model.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ClienteRepository extends JpaRepository<ClienteModel, Long> {
    // Buscar Email
    Optional<ClienteModel> findByEmail(String email);
    // Buscar por nome
    boolean existsByTelefone(String telefone);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    List<ClienteModel> findByNome(String nome);
    // Buscar Por id
    Optional<ClienteModel> findByIdCliente(Long idCliente);
}
