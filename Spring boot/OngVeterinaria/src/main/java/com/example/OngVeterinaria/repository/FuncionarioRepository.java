package com.example.OngVeterinaria.repository;

import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.FuncionarioModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuncionarioRepository extends JpaRepository<FuncionarioModel, Long> {
//   FuncionarioModel findByEmailAndPasswordFuncionario(String email, String passwordFuncionario);
   boolean existsByEmail(String email);
   FuncionarioModel findByEmail(String email);
}
