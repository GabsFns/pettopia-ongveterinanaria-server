# Usar uma imagem base estável para Java
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o arquivo JAR para o container
ARG JAR_FILE=target/OngVeterinaria.jar
COPY ${JAR_FILE} app.jar

# Expor a porta que o Spring Boot utiliza
EXPOSE 8081

# Comando para rodar a aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
