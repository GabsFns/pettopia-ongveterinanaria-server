package com.example.OngVeterinaria.controller;

import com.example.OngVeterinaria.DTO.ClienteDTO;
import com.example.OngVeterinaria.DTO.DenunciaDTO;
import com.example.OngVeterinaria.DTO.EnderecoDTO;
import com.example.OngVeterinaria.model.ClienteModel;
import com.example.OngVeterinaria.model.LoginRequest;
import com.example.OngVeterinaria.services.ClienteServices;
import com.example.OngVeterinaria.services.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/LoginWeb")
public class LoginClienteWeb {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private ClienteServices clienteServices;

    // Método para login e geração de token
    @Operation(
            summary = "Login e Geração de Token",
            description = "Este endpoint realiza o login de um cliente e gera um token JWT para acesso protegido.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso, token gerado."),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas.")
            }
    )
    // Método para login e geração de token
    @PostMapping("/loginWeb")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        ClienteModel usuario = clienteServices.findByEmail(loginRequest.getEmail());

        // Validação de email e senha
        if (usuario == null || !clienteServices.checkPassword(loginRequest.getPassword_Cliente(), usuario.getPassword_Cliente())) {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }

        // Converter ClienteModel para ClienteDTO, incluindo as denúncias e endereços
        ClienteDTO clienteDTO = clienteServices.convertToDTO(usuario);

        // Gerar token JWT
        String token = jwtService.generateToken(usuario.getEmail());
        AuthResponse response = new AuthResponse(token, clienteDTO);  // Retornar o token e o ClienteDTO com as denúncias

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Consulta Protegida",
            description = "Este endpoint permite realizar uma consulta protegida utilizando um token JWT.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Usuário não autorizado, token inválido.")
            }
    )
    // Método protegido que requer o token JWT
    @PostMapping("/consultaWeb")
    public ResponseEntity<?> realizarConsulta(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");

        // Validar token
        String email = jwtService.extractEmail(token);
        if (!jwtService.isTokenValid(token, email)) {
            return ResponseEntity.status(401).body("Usuário não autorizado.");
        }

        // Lógica para realizar a consulta
        return ResponseEntity.ok("Consulta realizada com sucesso!");
    }
}
