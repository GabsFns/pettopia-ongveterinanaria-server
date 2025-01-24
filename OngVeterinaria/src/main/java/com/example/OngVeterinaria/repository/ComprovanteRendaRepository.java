package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.ComprovanteRendaModel;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.PedidoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface ComprovanteRendaRepository extends JpaRepository <ComprovanteRendaModel, Long> {
    // Consulta para verificar se existe um pedido pendente para o cliente e animal
    Optional<ComprovanteRendaModel> findByCliente_IdClienteAndAnimal_IdAnimalAndStatus(Long idCliente, Long idAnimal, StatusGeral status);
    List<ComprovanteRendaModel> findByCliente_IdCliente(Long idCliente);
    List<ComprovanteRendaModel> findByStatusAndTempoExclusaoBefore(StatusGeral status, LocalDateTime tempoExclusao);
}
