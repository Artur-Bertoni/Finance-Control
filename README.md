# Controle Financeiro

## Objetivo
Desenvolver uma aplicação web para controle financeiro pessoal.

## Orientações para a Aplicação

### Orientações de inicialização
- Após clonar o repositório na máquina local, basta executar o [docker-compose](https://github.com/Artur-Bertoni/Finance-Control/blob/develop/docker-compose.yml) via interface ou via linha de comando, digitando `docker-compose up -d` (levando em conta que o terminal está localizado na [pasta raiz do projeto](https://github.com/Artur-Bertoni/Finance-Control));
- Agora basta rodar a aplicação normalmente e se atentar para os perfis disponíveis na aplicação, [dev](https://github.com/Artur-Bertoni/Finance-Control/blob/develop/src/main/resources/application-dev.yml) para desenvolvimento e [prd](https://github.com/Artur-Bertoni/Finance-Control/blob/develop/src/main/resources/application-prd.yml) para produção.

### Orientações para uso
- Para o uso devido da aplicação, certifique-se de que as portas 8080, 8081 e 5432 do seu computador não possuam nenhuma aplicação em execução, por estas são as portas que abrigarão, respectivamente, a aplicação em dev, em prd e o banco de dados.


### [Dependências Utilizadas](https://github.com/Artur-Bertoni/Finance-Control/blob/develop/pom.xml)
- [Spring Boot](https://spring.io/projects/spring-boot) (org.springframework.boot) -> utilizando os artefatos [spring-boot-starter-web](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web), [spring-boot-starter-data-jpa](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-jpa), [spring-boot-configuration-processor](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-configuration-processor) e [spring-boot-starter-test](https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test);
- [SpringDoc OpenAPI Starter WebMVC UI](https://springdoc.org/v2/) (org.springdoc) -> utilizando o artefato [springdoc-openapi-starter-webmvc-ui](https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui)
- [PostgreSQL JDBC Driver](https://www.postgresql.org) (org.postgresql) -> utilizando o artefato [postgresql](https://mvnrepository.com/artifact/org.postgresql/postgresql);
- [Liquibase](https://contribute.liquibase.com) (org.liquibase) -> utilizando o artefato [liquibase-core](https://mvnrepository.com/artifact/org.liquibase/liquibase-core);
- [Project Lombok](https://projectlombok.org) (org.projectlombok) -> utilizando o artefato [lombok](https://projectlombok.org/setup/maven);
- [MapStruct Core](https://mapstruct.org) (org.mapstruct) -> utilizando o artefato [mapstruct](https://mvnrepository.com/artifact/org.mapstruct/mapstruct).