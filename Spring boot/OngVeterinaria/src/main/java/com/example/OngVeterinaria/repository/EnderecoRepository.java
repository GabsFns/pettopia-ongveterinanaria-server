package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.EnderecoModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnderecoRepository extends JpaRepository <EnderecoModel, Long> {
    EnderecoModel findByLogradouroAndNumeroAndBairro(String logradouro, String numero, String bairro);
}
