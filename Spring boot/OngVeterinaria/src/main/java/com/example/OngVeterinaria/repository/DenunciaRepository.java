package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.DenunciaModel;
import com.example.OngVeterinaria.model.EnderecoModel;
import com.example.OngVeterinaria.model.Enum.StatusGeral;
import com.example.OngVeterinaria.model.Enum.TipoDenucias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DenunciaRepository extends JpaRepository <DenunciaModel, Long> {

    // Buscar denúncias por idCliente
    List<DenunciaModel> findByCliente_IdCliente(Long idCliente);

    // Buscar denúncias por data
    List<DenunciaModel> findByDataDenuncia(LocalDate dataDenuncia);

    long countByClienteIdCliente(Long idCliente);

    // Buscar denúncias por tipo
    List<DenunciaModel> findByTipoDenucias(TipoDenucias tipoDenucias);

    @Query("SELECT d FROM DenunciaModel d WHERE d.cliente.id = :idCliente AND d.dataDenuncia = :dataDenuncia AND d.tipoDenucias = :tipoDenucias")
    List<DenunciaModel> buscarPorClienteDataETipo(
            @Param("idCliente") Long idCliente,
            @Param("dataDenuncia") LocalDate dataDenuncia,
            @Param("tipoDenucias") TipoDenucias tipoDenucias
    );

    @Query("SELECT d.tipoDenucias, COUNT(d) FROM DenunciaModel d WHERE d.cliente.idCliente = :idCliente GROUP BY d.tipoDenucias")
    List<Object[]> countDenunciasByTipo(@Param("idCliente") Long idCliente);

    @Query("SELECT s FROM DenunciaModel s WHERE s.tipoDenucias = :tipo")
    List<DenunciaModel> findByTipo(@Param("tipo") TipoDenucias tipoDenucias);

    // Contar o número de denúncias por tipo
//    long countByTipoDenuncias(TipoDenucias tipoDenuncias);

    // Método corrigido para contar as denúncias por tipo
    @Query("SELECT COUNT(d) FROM DenunciaModel d WHERE d.tipoDenucias = :tipo")
    long countByTipoDenucias(@Param("tipo") TipoDenucias tipoDenucias);

    // Método para contar denúncias por tipo e status
    long countByTipoDenuciasAndStatusGeral(TipoDenucias tipo, StatusGeral status);

    @Query("SELECT COUNT(d) FROM DenunciaModel d WHERE d.statusGeral = 'PENDENTE'")
    long countDenunciasEmAndamento();

}