# SEALTH - Backend

Bem-vindo ao repositório do backend do projeto SEALTH (Sistema Eficaz de Administração Logística e Tecnologia Hospitalar). Este repositório contém o código-fonte do backend do SEALTH, responsável por fornecer os serviços necessários para o funcionamento da plataforma.

## Visão Geral

O backend do SEALTH é desenvolvido com o objetivo de fornecer uma infraestrutura robusta e segura para suportar as funcionalidades da plataforma. É responsável por gerenciar consultas, medicamentos, informações de saúde dos pacientes e outros aspectos relacionados à gestão hospitalar.

## Tecnologias Utilizadas

O backend do SEALTH é construído utilizando as seguintes tecnologias:

- Java
- Spring Framework (Spring Boot)
- Spring Data

### Banco de Dados

O backend utiliza um banco H2 em memória (`jdbc:h2:mem:testdb`): o schema é gerado pelo Hibernate a cada inicialização e os dados são perdidos ao reiniciar a aplicação.

### Configuração

| Variável | Padrão | Descrição |
|---|---|---|
| `JWT_SECRET` | valor de desenvolvimento | Segredo usado para assinar os tokens JWT (defina em produção) |
| `SEED_ENABLED` | `true` | Carrega dados de demonstração ao iniciar (`demo@sealth.com` e `maria@sealth.com`, senha `123456`) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origens permitidas, separadas por vírgula |
| `FRONTEND_URL` | `http://localhost:4200` | Endereço do front, usado no link de redefinição de senha |

## Rotas

Todas exigem token JWT, exceto as de `/auth` (login, cadastro e recuperação de senha). Os dados são sempre do usuário autenticado (acessar o recurso de outro usuário devolve 404). Os detalhes de cada rota estão no Swagger.

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/register`, `POST /auth/login`, `POST /auth/forgot-password`, `POST /auth/reset-password` |
| Usuário | `GET /user/{id}`, `PUT /user/update/{id}` (só o próprio usuário) |
| Consultas | `POST /appointments/create`, `GET /appointments/all`, `PUT /appointments/{id}/update`, `DELETE /appointments/{id}/cancel`, `GET /appointments/reminders` (horário e lembrete opcionais) |
| Saúde | `GET /health/defaults`, `POST/GET /health/records`, `GET /health/records/latest`, `DELETE /health/records/{id}` |
| Medicamentos | `GET /medicines/pdf` (bulas), `POST/GET /medicines/mine`, `PUT/DELETE /medicines/mine/{id}` |
| Vacinas | `GET /vaccines` (calendário e situação), `GET /vaccines/alerts`, `POST/GET /vaccines/records`, `DELETE /vaccines/records/{id}` |

### Recuperação de senha

1. `POST /auth/forgot-password` com o e-mail gera um link (`<FRONTEND_URL>/reset-password?token=...`) válido por 30 minutos. A resposta é sempre a mesma, exista ou não conta com aquele e-mail.
2. `POST /auth/reset-password` com `token` e `password` define a nova senha. O link só funciona uma vez, e um pedido novo invalida o anterior. Só o hash do token fica no banco.

**Não há servidor de e-mail configurado:** em desenvolvimento o link é escrito no log da aplicação (`LoggingPasswordResetNotifier`). Para enviar e-mails de verdade, implemente `PasswordResetNotifier`. Os tokens JWT já emitidos continuam valendo até expirar (duas horas).

O calendário de vacinação é um resumo informativo (`VaccineCatalog`) e não substitui a orientação de um profissional de saúde.

## Instalação

Para executar o backend do SEALTH localmente (requer JDK 17+), siga estas etapas:

1. Clone este repositório para o seu ambiente de desenvolvimento
2. Navegue até `sealth-api`
3. Execute `./mvnw spring-boot:run` (a API sobe em `http://localhost:8080`)

### Documentação (Swagger)

Com a API no ar, acesse `http://localhost:8080/swagger-ui.html` (especificação em `/v3/api-docs`). Para testar endpoints protegidos, faça login em `/auth/login` e informe o token em **Authorize**.

### Testes e cobertura

```
./mvnw test
```

O relatório de cobertura (JaCoCo) é gerado em `target/site/jacoco/index.html`.

Os erros da API seguem o formato `{timestamp, status, error, message, fieldErrors?}`.

## Contribuições

Contribuições são bem-vindas! Se você encontrar algum problema ou tiver sugestões de melhorias, sinta-se à vontade para abrir uma issue ou enviar um pull request.

## Equipe

- [Lucas Chagas](https://github.com/llucaschagass/)

## Licença

Este projeto é licenciado sob a MIT License.
