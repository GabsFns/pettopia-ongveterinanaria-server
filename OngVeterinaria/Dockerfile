# Usar uma imagem base estável para Java
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho dentro do container
WORKDIR /app

RUN apt-get update && apt-get install -y netcat && apt-get clean

# Copia o arquivo JAR para o container
ARG JAR_FILE=target/OngVeterinaria.jar
COPY ${JAR_FILE} app.jar

ADD wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# Expor a porta que o Spring Boot utiliza
EXPOSE 8081

# Comando para rodar a aplicação
ENTRYPOINT ["./wait-for-it.sh", "db:3306", "--", "java", "-jar", "/app/app.jar"]