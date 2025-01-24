package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.PedidoModel;
import com.example.OngVeterinaria.model.Enum.PedidosTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository <PedidoModel, Long> {
    // Definir o método de consulta para buscar pedido de adoção com base no ID do animal e ID do cliente
    Optional<PedidoModel> findByAnimal_IdAnimalAndCliente_IdClienteAndTipo(Long idAnimal, Long idCliente, PedidosTipo tipo);
    PedidoModel findByCodigoComprovante(String codigoComprovante);
    List<PedidoModel> findByCliente_IdCliente(Long idCliente);
    Optional <PedidoModel> findByAnimal_IdAnimal(Long idAnimal);
    @Query("SELECT s FROM PedidoModel s WHERE s.tipo = :tipo")
    List<PedidoModel> findByTipo(@Param("tipo") PedidosTipo tipo);
    @Query("SELECT p FROM PedidoModel p WHERE p.animal.idAnimal = :idAnimal AND p.cliente.idCliente = :idCliente AND p.StatusPedido = :statusPedido")
    Optional<PedidoModel> findByAnimal_IdAnimalAndCliente_IdClienteAndStatusPedido(
            @Param("idAnimal") Long idAnimal,
            @Param("idCliente") Long idCliente,
            @Param("statusPedido") StatusGeral statusPedido
    );

    List<PedidoModel> findByTipoAndValido(PedidosTipo tipo, Boolean valido);

    @Query("SELECT p FROM PedidoModel p WHERE p.tipo = :tipo AND p.StatusPedido = :status")
    List<PedidoModel> findByTipoAndStatusPedido(@Param("tipo") PedidosTipo tipo, @Param("status") StatusGeral status);

    @Query("SELECT " +
            "COUNT(CASE WHEN p.tipo = 'DOACAO' AND p.StatusPedido = 'ANDAMENTO' THEN 1 END) AS doacoesEmAndamento, " +
            "COUNT(CASE WHEN p.tipo = 'ADOCAO' AND p.StatusPedido = 'ANDAMENTO' THEN 1 END) AS adocoesEmAndamento " +
            "FROM PedidoModel p")
    Map<String, Long> countDoacoesAndAdocoesEmAndamento();
}
