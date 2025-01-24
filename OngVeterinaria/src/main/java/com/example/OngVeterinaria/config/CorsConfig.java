package com.example.OngVeterinaria.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfiguracao() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registro) {
                registro.addMapping("/**") // Permite todas as rotas
                        .allowedOriginPatterns("http://localhost:**", "http://localhost:3000", "http://localhost:8080", "http://localhost") // Add http://localhost to the list of allowed origins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Permite métodos específicos
                        .allowedHeaders("*") // Permite todos os headers
                        .allowCredentials(true); // Permite envio de credenciais (cookies, headers de autenticação)
            }
        };
    }
}
