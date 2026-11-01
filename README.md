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
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origens permitidas, separadas por vírgula |

## Instalação

Para executar o backend do SEALTH localmente (requer JDK 17+), siga estas etapas:

1. Clone este repositório para o seu ambiente de desenvolvimento
2. Navegue até `sealth-api`
3. Execute `./mvnw spring-boot:run` (a API sobe em `http://localhost:8080`)

Os erros da API seguem o formato `{timestamp, status, error, message, fieldErrors?}`.

## Contribuições

Contribuições são bem-vindas! Se você encontrar algum problema ou tiver sugestões de melhorias, sinta-se à vontade para abrir uma issue ou enviar um pull request.

## Equipe

- [Lucas Chagas](https://github.com/llucaschagass/)

## Licença

Este projeto é licenciado sob a MIT License.
