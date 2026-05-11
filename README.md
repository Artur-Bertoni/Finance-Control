# Finance Control

Aplicação web de controle financeiro pessoal. Permite gerenciar contas bancárias, categorias, transações, transferências, locais de transação e instituições financeiras — tudo vinculado a um usuário autenticado.

Iniciado em 2023/2 como projeto da cadeira de Implementação de Aplicações Especiais, originalmente com PHP, HTML, CSS, JavaScript e MySQL. Após a conclusão da cadeira, o projeto continuou em desenvolvimento pessoal e passou por uma reestruturação completa: o backend foi reescrito em Java com Spring Boot e o frontend reorganizado em uma arquitetura modular de páginas estáticas.

---

## Tecnologias

| Camada | Tecnologia | Versão |
|---|---|---|
| Backend | Java + Spring Boot | 17 / 3.5.x |
| Persistência | Spring Data JPA + Hibernate | — |
| Banco de dados | MySQL | 9.x |
| Frontend | HTML + CSS + JavaScript (ES6 modules) | — |
| HTTP client | jQuery | 3.6.0 |
| Proxy (Docker) | Nginx | alpine |
| Proxy (local) | Node.js | — |
| Build | Maven | 3.x |

---

## Pré-requisitos

### Para rodar com Docker (recomendado)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) com Docker Compose

### Para rodar localmente (debug)
- [JDK 17](https://adoptium.net/pt-BR/temurin/releases?version=17&os=any&arch=any)
- [Maven 3.x](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/products/docker-desktop/)
- [Node.js 24.x](https://nodejs.org/pt-br/download)

---

## Rodando com Docker

```bash
docker compose up --build
```

Acesse em: **http://localhost:8080**

Para parar:
```bash
docker compose down
```

Para destruir os dados do banco também:
```bash
docker compose down -v
```

> Na primeira execução o banco é inicializado automaticamente pelo backend usando Liquibase.

---

## Versionamento de banco de dados

O projeto usa Liquibase para migrar o banco automaticamente. Os changelogs SQL ficam em:

- `backend/src/main/resources/db/db.changelog-master.sql`
- `backend/src/main/resources/db/changelog/*.sql`

Isso garante que as alterações de esquema sejam aplicadas na inicialização do backend e que cada changeset seja executado apenas uma vez.

---

## Rodando localmente (com debug)

### 1. Subir apenas o banco de dados

```bash
docker compose up db -d
```

O MySQL ficará disponível em `localhost:3306` com as credenciais:

| Propriedade | Valor |
|---|---|
| Database | `finance-control` |
| Usuário | `user` |
| Senha | `test` |

### 2. Compilar o backend

```bash
cd backend
mvn clean compile
```

### 3. Iniciar o backend em modo debug

**VS Code** — crie `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Finance Control",
      "request": "launch",
      "mainClass": "com.financecontrol.FinanceControlApplication",
      "projectName": "finance-control"
    }
  ]
}
```
Pressione `F5`.

**IntelliJ IDEA** — abra a pasta `backend/`, localize `FinanceControlApplication.java` e clique no ícone de debug ao lado do método `main`.

O backend sobe em: **http://localhost:8081**

### 4. Subir o servidor proxy do frontend

O frontend usa caminhos relativos como `/api/` e `/pages/`. O `proxy-server.js` serve os arquivos estáticos e faz proxy das chamadas de API para o backend:

```bash
# na raiz do projeto
node proxy-server.js
```

Acesse em: **http://localhost:8080**

### Resumo do fluxo local

```
Browser :8080  →  proxy-server.js  →  /api/*  →  Spring Boot :8081  →  MySQL :3306
                                    →  /*      →  frontend/ (arquivos estáticos)
```

---

## Primeiro acesso

O banco inicia vazio (sem usuários). Para acessar o sistema:

1. Acesse **http://localhost:8080/pages/User.html**
2. Preencha os dados e clique em **Criar Conta**
3. O login é feito automaticamente após o cadastro

> A autenticação é baseada em sessão HTTP. A senha é armazenada com BCrypt.

---

## Organização de pastas

```
Finance-Control/
├── backend/                        # Aplicação Spring Boot
│   ├── src/main/java/com/financecontrol/
│   │   ├── config/                 # Configurações (CORS, PasswordEncoder)
│   │   ├── controller/             # Endpoints REST
│   │   ├── dto/
│   │   │   ├── request/            # Payloads de entrada
│   │   │   └── response/           # Payloads de saída
│   │   ├── entity/                 # Entidades JPA
│   │   ├── exception/              # Exceções customizadas + GlobalExceptionHandler
│   │   ├── repository/             # Interfaces Spring Data JPA
│   │   └── service/                # Regras de negócio
│   ├── src/main/resources/
│   │   └── application.properties  # Configurações da aplicação
│   └── pom.xml
│
├── frontend/
│   ├── pages/                      # Páginas HTML (uma por tela)
│   ├── js/
│   │   ├── *.js                    # Módulo JS de cada página
│   │   ├── ThemeManager.js         # Dark/light mode
│   │   ├── components/             # Componentes reutilizáveis (Sidebar, PasswordInput)
│   │   ├── icons/                  # Biblioteca de ícones SVG inline
│   │   └── utils/                  # Funções utilitárias compartilhadas
│   ├── styles/
│   │   └── styles.css              # Estilos globais (inclui dark mode via data-theme)
│   └── images/                     # Assets estáticos
│
├── docker-compose.yml              # Orquestração Docker (db + backend + nginx)
├── nginx.conf                      # Configuração do proxy Nginx (Docker)
└── proxy-server.js                 # Servidor proxy Node.js (desenvolvimento local)
```

### Endpoints disponíveis

| Recurso | Base URL |
|---|---|
| Autenticação | `/api/auth` |
| Usuários | `/api/users` |
| Contas | `/api/accounts` |
| Categorias | `/api/categories` |
| Transações | `/api/transactions` |
| Transferências | `/api/transfers` |
| Locais | `/api/transaction-locales` |
| Instituições Financeiras | `/api/financial-institutions` |

Todos os endpoints (exceto login e criação de usuário) exigem sessão autenticada.
