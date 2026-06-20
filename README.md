# Finance Control

Aplicação web de controle financeiro pessoal. Permite gerenciar contas bancárias, categorias, transações, transferências, locais de transação e instituições financeiras - tudo vinculado a um usuário autenticado.

Iniciado em 2023/2 como projeto da cadeira de Implementação de Aplicações Especiais, originalmente com PHP, HTML, CSS, JavaScript e MySQL. Após a conclusão da cadeira, o projeto continuou em desenvolvimento pessoal e passou por uma reestruturação completa: o backend foi reescrito em Java com Spring Boot e o frontend reorganizado em uma arquitetura modular de páginas estáticas.

---

## Tecnologias

| Camada | Tecnologia | Versão |
|---|---|---|
| Backend | Java + Spring Boot | 17 / 3.5.x |
| Persistência | Spring Data JPA + Hibernate | - |
| Banco de dados | MySQL | 8.0 |
| Migrações | Liquibase | - |
| Segurança | Spring Security + JWT (stateless) + OAuth2 (Google) | - |
| Cache | Spring Cache + Caffeine | - |
| Importação de extrato | Apache PDFBox | 3.0.x |
| E-mail | Spring Mail (SMTP) | - |
| Frontend | HTML + CSS + JavaScript (ES6 modules, SPA) | - |
| HTTP client | jQuery | 3.6.0 |
| Deploy (produção) | Railway - 1 serviço web (Spring serve front + API) + MySQL | - |
| Proxy (dev local) | Node.js | - |
| Proxy / TLS (self-host) | Nginx | alpine |
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

> **Produção roda no Railway** (1 serviço web + MySQL) - ver [Deploy em produção](#deploy-em-produção).
> A stack `docker-compose` abaixo (`db` + `api` + `web`/Nginx com TLS) é a opção de **self-host**
> em VPS própria. Para desenvolver no dia a dia, use [Rodando localmente](#rodando-localmente-com-debug),
> que sobe só o banco via Docker.

O `docker-compose.yml` lê as variáveis de um arquivo `.env`. Crie o seu a partir do exemplo:

```bash
cp .env.example .env   # depois edite senhas, segredos e domínio
```

```bash
docker compose up --build
```

Para parar:
```bash
docker compose down
```

Para destruir os dados do banco também:
```bash
docker compose down -v
```

> Na primeira execução o esquema é criado automaticamente pelo Liquibase na subida do backend.

---

## Versionamento de banco de dados

O projeto usa Liquibase para migrar o banco automaticamente. Os changelogs SQL ficam em:

- `backend/src/main/resources/db/db.changelog-master.xml`
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

**VS Code** - crie `.vscode/launch.json`:
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

**IntelliJ IDEA** - abra a pasta `backend/`, localize `FinanceControlApplication.java` e clique no ícone de debug ao lado do método `main`.

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

## Deploy em produção

O sistema roda no **Railway**, com **dois serviços**:

- **web** - buildado a partir de `Dockerfile.railway`: o Maven empacota o backend e copia `frontend/` para `resources/static`; assim o **Spring Boot serve o frontend e a API no mesmo processo** (escuta na porta `$PORT` injetada pelo Railway; o TLS é terminado na borda do Railway, sem Nginx).
- **MySQL** - banco gerenciado (plugin do Railway).

### Configuração

1. Conecte o repositório Git ao serviço **web** e aponte o Dockerfile para `Dockerfile.railway`.
2. Defina as variáveis de ambiente no painel do serviço web (não há `.env` em produção): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `APP_BASE_URL`, SMTP e - se usar login social - `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET`. Use o **hostname interno** do MySQL (`*.railway.internal`) em `DB_HOST` para evitar a latência do proxy público.
3. Cada push na branch dispara um novo deploy.

### Otimizações de performance ativas

- **gzip** via Spring (`server.compression`) - a borda do Railway não comprime, então é isto que reduz CSS/JS/JSON/locales no primeiro carregamento.
- **Cache de estáticos** (`spring.web.resources.cache`, `max-age` 1h) com revalidação 304 por `Last-Modified`.
- **JVM** (`Dockerfile.railway`): `MaxRAMPercentage`, `SerialGC` e `TieredStopAtLevel=1` para menor uso de memória e cold start mais rápido em 1 vCPU.
- **Backend**: cache de leitura com Caffeine (TTL 10–30 min), `open-in-view=false` e pool HikariCP dimensionado.

> Boa parte da lentidão percebida em planos pequenos vem de fatores de infra, não do código: se o **App Sleeping** estiver ativo, a instância hiberna e o **cold start da JVM** torna o primeiro acesso lento - desative o sleeping (ou use um plano sem hibernação). Garanta também que **web e MySQL estejam na mesma região**.

### Acessar o banco de produção (Railway CLI)

O MySQL não fica exposto publicamente. Use o CLI do Railway a partir da raiz do projeto:

```bash
npm i -g @railway/cli      # instala o CLI (uma vez)
railway login
railway link               # vincula a pasta ao projeto Railway
railway connect MySQL      # abre uma sessão mysql no banco de produção
```

> Para um cliente gráfico (DBeaver/MySQL Workbench), copie as variáveis de conexão do serviço MySQL no painel do Railway (TCP proxy público) e conecte com elas.

### Self-host alternativo (VPS com Docker)

Como alternativa ao Railway, dá para auto-hospedar com a stack `docker-compose` (Nginx + TLS):

1. **Variáveis** - preencha o `.env` (ver `.env.example`).
2. **Domínio e TLS** - em `nginx.conf`, troque `seu-dominio.com` e emita o certificado: `sudo certbot --nginx -d seu-dominio.com` (o volume `/etc/letsencrypt` já é montado no container `web`).
3. **Suba** - `docker compose up --build -d`.
4. **Banco** - o MySQL é publicado só no loopback (`127.0.0.1:3306`); abra um túnel SSH com `scripts/db-tunnel.ps1 -Server usuario@servidor` e conecte um cliente em `127.0.0.1:3307`.

---

## Primeiro acesso

O banco inicia vazio (sem usuários). Para acessar o sistema:

1. Acesse **http://localhost:8080/pages/Register.html** e crie sua conta - ou use **Continuar com Google** na tela de Login (requer OAuth2 configurado)
2. Após o cadastro/login você é levado ao app (`/pages/AppShell.html#/dashboard`)

> A autenticação é **stateless via JWT**; o login social usa **OAuth2 (Google)**. A senha é armazenada com BCrypt. Cadastros por e-mail passam por verificação de e-mail.

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
├── frontend/                       # SPA (shell + fragmentos HTML por rota)
│   ├── pages/
│   │   ├── AppShell.html           # Shell da SPA (roteamento por hash: #/dashboard…)
│   │   ├── Login.html / Register.html / Dashboard.html / …
│   │   ├── crud/                   # Fragmentos de formulário (criar/editar)
│   │   ├── lists/                  # Fragmentos de listagem
│   │   ├── views/                  # Fragmentos de detalhe
│   │   └── admin/                  # Telas administrativas (ex.: feedbacks)
│   ├── js/
│   │   ├── AppShell.js             # Roteador da SPA + injeção de sidebar/conteúdo
│   │   ├── ThemeManager.js         # Dark/light mode
│   │   ├── components/             # Componentes reutilizáveis (Sidebar, Mascot, selects…)
│   │   ├── modals/                 # Modais (confirmação, quick add, conflitos, loading)
│   │   ├── icons/                  # Biblioteca de ícones SVG inline
│   │   ├── class/                  # Wrappers de entidades (AccountClass, etc.)
│   │   ├── admin/                  # Lógica das telas administrativas
│   │   └── utils/                  # Funções utilitárias compartilhadas
│   ├── locales/                    # Traduções i18n (pt.json, en.json, es.json)
│   ├── styles/                     # styles.css + fontes (Phosphor)
│   └── images/                     # Assets estáticos
│
├── Dockerfile.railway              # Build de produção (Railway): Spring serve front + API
├── scripts/
│   └── db-tunnel.ps1               # [self-host] Túnel SSH p/ o MySQL em VPS
├── .env.example                    # Modelo das variáveis (dev / self-host)
├── docker-compose.yml              # [self-host] Stack Docker (db + api + nginx/TLS)
├── nginx.conf                      # [self-host] Nginx: TLS, gzip, HTTP/2, cache, proxy /api
└── proxy-server.js                 # Servidor proxy Node.js (desenvolvimento local)
```

### Endpoints disponíveis

| Recurso | Base URL |
|---|---|
| Autenticação (login, logout, /me, verificação de e-mail, vínculo Google) | `/api/auth` |
| Login social (OAuth2 Google) | `/oauth2/**`, `/login/oauth2/**` |
| Usuários | `/api/users` |
| Contas | `/api/accounts` |
| Categorias | `/api/categories` |
| Transações | `/api/transactions` |
| Transferências | `/api/transfers` |
| Locais | `/api/transaction-locales` |
| Instituições Financeiras | `/api/financial-institutions` |
| Metas Financeiras | `/api/goals` |
| Conquistas | `/api/achievements` |
| Notificações in-app | `/api/notifications` |
| Importação de extrato (PDF) | `/api/statements` |
| Histórico de alterações | `/api/change-history` |
| Feedback do usuário | `/api/feedback` |
| Relatórios / Dashboard | `/api/reports` |
| Admin (feedbacks, e-mail de teste) | `/api/admin/**` |
| Health check | `/actuator/health` |

Todos os endpoints exigem JWT válido, exceto login, criação de usuário e o fluxo OAuth2.

---

## Funcionalidades principais

- **Autenticação** - cadastro com verificação de e-mail, login por e-mail/senha (BCrypt) ou Google (OAuth2); sessão stateless via JWT
- **Transações** - registro de débitos e créditos com categorias, contas, locais e parcelas; transferências entre contas
- **Importação de extrato** - upload de PDFs bancários (Apache PDFBox) com pré-visualização, detecção de duplicatas e resolução de conflitos antes de confirmar
- **Dashboard** - gráficos de receitas vs despesas com filtros por período, categoria e conta (resultado cacheado)
- **Metas Financeiras** - metas de limite de gastos, economia e receita com acompanhamento de progresso e notificações
- **Conquistas** - sistema de gamificação baseado em hábitos financeiros
- **Notificações in-app** - pop-ups ao atingir marcos de metas, histórico persistido no banco; e-mail para aviso de prazo (7 dias)
- **Histórico de alterações** - auditoria de mudanças por entidade
- **Feedback do usuário** - envio de feedback com painel administrativo de leitura
- **Mascote Finny** - porquinho cofre como botão flutuante (FAB) com painel de dicas financeiras e central de notificações; dicas rotativas a cada 30 minutos com countdown; página dedicada `/pages/FinnyCenter.html`
- **Multi-idioma** - interface em Português, Inglês e Espanhol
- **Tema escuro/claro** - alternância de tema com persistência via localStorage
- **Atalhos de teclado** - `Alt+1`–`0` e `Alt+Q`–`P` para navegação direta pelos itens da sidebar
