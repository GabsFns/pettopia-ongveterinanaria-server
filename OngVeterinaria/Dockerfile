# Usa uma imagem base do Java 17 baseada no Debian
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Atualiza os pacotes e instala as bibliotecas necessárias
RUN apt-get update && \
    apt-get install -y fontconfig libfreetype6 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Define o argumento para o caminho do JAR
ARG JAR_FILE=target/OngVeterinaria.jar

# Copia o arquivo JAR para o container
COPY ${JAR_FILE} app.jar

# Adiciona o script wait-for-it.sh ao container
ADD wait-for-it.sh /app/wait-for-it.sh

# Torna o script executável
RUN chmod +x /app/wait-for-it.sh

# Expõe a porta 8081
EXPOSE 8081

# Define o ponto de entrada do container
ENTRYPOINT ["./wait-for-it.sh", "db:3306", "--", "java", "-jar", "/app/app.jar"]