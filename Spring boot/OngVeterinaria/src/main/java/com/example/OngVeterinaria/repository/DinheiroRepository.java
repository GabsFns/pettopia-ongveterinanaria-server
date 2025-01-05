package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.DinheiroModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DinheiroRepository extends JpaRepository<DinheiroModel, Long> {
    @Query("SELECT EXTRACT(YEAR FROM d.dataDoacao) AS ano, EXTRACT(MONTH FROM d.dataDoacao) AS mes, SUM(d.valor) AS totalDoado " +
            "FROM DinheiroModel d WHERE d.cliente.idCliente = :idCliente " +
            "GROUP BY EXTRACT(YEAR FROM d.dataDoacao), EXTRACT(MONTH FROM d.dataDoacao) " +
            "ORDER BY ano, mes")
    List<Object[]> findDoacoesPorMes(@Param("idCliente") Long idCliente);

    List<DinheiroModel> findByClienteIdCliente(Long idCliente);
}
