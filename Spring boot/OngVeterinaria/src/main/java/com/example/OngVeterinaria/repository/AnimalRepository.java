package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.AnimalModel;
import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.Enum.TipoEspecie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnimalRepository extends JpaRepository<AnimalModel, Long> {

    boolean existsByIdAnimalAndAdocaoTrue(Long id);
    // Método para buscar os animais com id_cliente == null
    List<AnimalModel> findByClienteIsNull();
    List<AnimalModel> findByCliente_IdCliente(Long idCliente);
    List<AnimalModel> findByAdocaoTrue();
    //@Query faz filtragem dentro das tabelas SQL
    @Query("SELECT a FROM AnimalModel a " +
            "WHERE (:especie IS NULL OR a.especie = :especie) " +
            "AND (:raca IS NULL OR a.raca = :raca)")
    List<AnimalModel> findByFilters(@Param("especie") TipoEspecie especie,
                                    @Param("raca") String raca);
    Optional<AnimalModel> findByIdAnimalAndCliente(Long idAnimal, ClienteModel cliente);
}

//WHERE (:especie IS NULL OR a.especie = :especie):
//        (:especie IS NULL OR a.especie = :especie): Este é um critério de filtro. A consulta retorna registros onde:
//Se o parâmetro especie for NULL, a condição será true para todos os registros (ou seja, o filtro por espécie será ignorado).
//        Caso contrário, apenas registros onde o campo especie da entidade AnimalModel seja igual ao valor de :especie serão retornados.
//AND (:raca IS NULL OR a.raca = :raca):
//        (:raca IS NULL OR a.raca = :raca): Este é outro critério de filtro. A consulta retorna registros onde:
//Se o parâmetro raca for NULL, a condição será true para todos os registros (ou seja, o filtro por raça será ignorado).
//        Caso contrário, apenas registros onde o campo raca da entidade AnimalModel seja igual ao valor de :raca serão retornados