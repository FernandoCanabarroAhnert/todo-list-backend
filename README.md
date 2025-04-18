# Projeto Full Stack: Todo List 📝 (back-end)

![Java](https://img.shields.io/badge/java-FF5722.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-0B3D30?style=for-the-badge&logo=mongodb&logoColor=white)
![Atlas](https://img.shields.io/badge/Mongo%20Express-285C35?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![SendGrid](https://img.shields.io/badge/SendGrid-00BFFF?style=for-the-badge&logo=maildotru&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-F80000?style=for-the-badge&logo=openid&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-%2300BCD4?style=for-the-badge&logo=Docker&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-%2325A162?style=for-the-badge&logo=JUnit5&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-%238C4C00?style=for-the-badge&logo=Codecov&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-F80000?style=for-the-badge&logo=oracle&logoColor=white)

## O que é o projeto? 🤔

O projeto é o back-end de uma simples aplicação de uma todo-list. É um projeto relativamente genérico, mas eu o fiz com a intenção de ser o meu 1o projeto full-stack utilizando Spring Boot e Angular. Por mais que seja simples, não quer dizer que não tenha coisas interessantes.
A aplicação conta com Testes Unitários com Mockito e Testes de Integração com TestContainers. Uma pipeline de CI/CD com Github Actions, e deploy no OCI (Oracle Cloud Infrastructure) com Traefik, possibilitando o uso de domínios próprios (que no caso, foi o fernandocanabarrodev.tech) e HTTPS, tanto para o Front-end quanto para o Back-end.
Além disso, tem autorização e autenticação com JWT e OAuth2 ,e o banco de dados MongoDB instanciado no Atlas, que é um serviço que fornece bancos de dados do MongoDB na nuvem.

## Tecnologias 💻
 
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [JWT](https://jwt.io/)
- [OAuth2](https://oauth.net/2/)
- [Docker](https://www.docker.com/)
- [SendGrid](https://sendgrid.com/en-us)
- [GithubActions](https://docs.github.com/pt/actions)
- [Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
- [JUnit5](https://junit.org/junit5/)
- [Mockito](https://site.mockito.org/)
- [MockMvc](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
- [Jacoco](https://www.eclemma.org/jacoco/)
- [TestContainers](https://testcontainers.com/)
- [GithubActions](https://docs.github.com/pt/actions)
- [Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)


## Como executar 🎉

1.Clonar repositório git:

```text
git clone https://github.com/FernandoCanabarroAhnert/todo-list-backend.git
```

2.Instalar dependências.

```text
mvn clean install
```

3.Executar a aplicação Spring Boot.

### Usando Docker 🐳

- Clonar repositório git
- Construir o projeto:
```
./mvnw clean package
```
- Construir a imagem:
```
./mvnw spring-boot:build-image
```
- Executar o container:
```
docker run --name todo-list-backend -p 8080:8080  -d todo-list-backend:0.0.1-SNAPSHOT
```