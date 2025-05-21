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

### 👨‍💻 Desenvolvedores

- **GabsFns**: Desenvolvedor principal do backend
- **daeldev**: Contribuidor do backend

---
