# 🐳 Como Executar o Backend com Docker

Siga os passos abaixo para subir o backend do **PetTopia** com **Docker**.

---

### ✅ Pré-requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop) instalado  
- [Git](https://git-scm.com/downloads) instalado

---

### 📥 1. Baixe o Repositório

Você pode **baixar ou clonar** o projeto da seguinte forma:

#### 🔁 **Opção 1: Clonar com Git**
> *Requer Git instalado*

1. Abra o terminal no seu computador.
2. Execute o seguinte comando para clonar o repositório:

```bash
git clone https://github.com/GabsFns/pettopia-ongveterinanaria-server.git
```

#### 📦 **Opção 2: Baixar ZIP**

1. Acesse o repositório no GitHub: [Clique aqui para baixar o ZIP](https://github.com/GabsFns/pettopia-ongveterinanaria-server/archive/refs/heads/main.zip).
2. Extraia o arquivo `.zip` no seu computador.
3. Acesse a pasta do projeto:

```bash
cd pettopia-ongveterinanaria-server\OngVeterinaria
```

---

### 🚀 2. Suba os Contêineres com Docker

Agora que o repositório está pronto, é hora de subir o servidor. Para isso, execute o seguinte comando dentro da pasta do projeto:

```bash
docker-compose up --build
```
Isso criará o ambiente com o MySQL e o Spring Boot, construindo e iniciando o backend automaticamente.

Aguarde alguns segundos até que o servidor esteja totalmente rodando. Após o processo, o backend estará disponível em http://localhost:8081.

Agora, com o backend em execução, fique à vontade para testar a plataforma web/mobile/java desktop!

---

### 🛠 &nbsp;**Tecnologias Utilizadas**
- **JDK 17**: Versão do Java Development Kit.
- **Spring Boot**: Framework usado para criação da API REST.
- **Spring Security + JWT**: Autenticação e autorização.
- **Spring Data JPA + Hibernate**: Persistência de dados com ORM.
- **XChart & JFreeChart**: Bibliotecas para geração de gráficos.
- **iText**: Biblioteca usada para geração de relatórios em PDF.
- **Barcode4J**: Geração de código de barras.
- **MySQL**: Banco de dados relacional.
- **Swagger / OpenAPI**: Documentação interativa da API.
- **Apache Maven**: Gerenciador de dependências e build.
- **Docker**: Contêinerização do backend e banco de dados.
- **Postman**: Testes de API.
- **UML**: Modelagem de sistemas com diagramas.

<p height="">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=flat&logo=openjdk&logoColor=white" height=30, title="Java"/>
  <img src="https://img.shields.io/badge/Spring-6DB33F.svg?style=for-the-badge&logo=Spring&logoColor=white" height=30, title="Spring Framework"/>
  <img src="https://img.shields.io/badge/Hibernate-59666C.svg?style=for-the-badge&logo=Hibernate&logoColor=white" height=30, title="Hibernate"/>
  <img src="https://img.shields.io/badge/GitHub-181717.svg?style=for-the-badge&logo=GitHub&logoColor=white" height=30, , title="GitHub"/>
  <img src="https://img.shields.io/badge/Git-F05032.svg?style=for-the-badge&logo=Git&logoColor=white" height=30, title="Git"/>
  <img src="https://img.shields.io/badge/Docker-2496ED.svg?style=for-the-badge&logo=Docker&logoColor=white" height=30, title="Docker"/>
  <img src="https://img.shields.io/badge/Swagger-85EA2D.svg?style=for-the-badge&logo=Swagger&logoColor=black" height=30, title="Swagger"/>
  <img src="https://img.shields.io/badge/Apache%20Maven-C71A36.svg?style=for-the-badge&logo=Apache-Maven&logoColor=white" height=30, title="Apache Maven"/>
  <img src="https://img.shields.io/badge/Postman-FF6C37.svg?style=for-the-badge&logo=Postman&logoColor=white" height=30, title="Postman"/>
  <img src="https://img.shields.io/badge/UML-FABD14.svg?style=for-the-badge&logo=UML&logoColor=black" height=30, title="UML"/>
  <img src="https://img.shields.io/badge/IntelliJ%20IDEA-000000.svg?style=for-the-badge&logo=IntelliJ-IDEA&logoColor=white" height=30, title="IntelliJ IDE"/>
</p>

---

### 👨‍💻 Desenvolvedores

- **GabsFns**: Desenvolvedor principal do backend
- **daeldev**: Contribuidor do backend

---
