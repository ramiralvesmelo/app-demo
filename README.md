# 📘 App Demo ERP

Aplicação de demonstração de um sistema ERP simplificado, desenvolvida em **Spring Boot 2.7**, com **Hibernate (JPA)**, **validação de dados** e banco em memória **H2**.

Este projeto foi criado como **base de estudo** e para demonstrar **boas práticas em arquitetura Java**, **persistência de dados** e **testes automatizados**.

---

## 🚀 Tecnologias Utilizadas

* **Java 11**
* **Spring Boot 2.7.18**

  * Spring Web
  * Spring Data JPA
  * Spring Validation
* **H2 Database (runtime)**
* **Hibernate ORM**
* **Lombok**
* **JUnit 4 e 5**
* **Mockito**
* **Maven 3.8+**

---

## ⚙️ Pré-requisitos

* [Java 11+](https://adoptium.net/)
* [Maven 3.8+](https://maven.apache.org/)
* IDE recomendada: **Eclipse STS** ou **IntelliJ IDEA** com suporte a Spring Boot.

---

## 📦 Como Compilar e Executar

Clone o repositório e execute os comandos abaixo:

```bash
# Clonar o projeto
git clone https://github.com/seu-usuario/app-demo.git
cd app-demo

# Compilar
mvn clean install

# Executar a aplicação
mvn spring-boot:run
```

A aplicação será iniciada em:
👉 [http://localhost:8080](http://localhost:8080)

---

## 🗄️ Banco de Dados (H2 Console)

O projeto utiliza o **H2 Database em memória** para facilitar testes e execução local.

Acesse o console H2 em:
👉 [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Configuração padrão:

* **JDBC URL:** `jdbc:h2:mem:testdb`
* **Usuário:** `sa`
* **Senha:** *(vazio)*

---

## ✅ Testes

O projeto inclui testes **unitários e de integração** utilizando **JUnit 4/5** e **Mockito**.

Para rodar os testes:

```bash
mvn test
```

---

## 🗂️ Estrutura do Projeto

```
app-demo
 ├── src/main/java/br/com/springboot/erp
 │    ├── controller    # Camada de exposição (REST Controllers)
 │    ├── model         # Entidades JPA
 │    ├── repository    # Repositórios JPA
 │    ├── service       # Regras de negócio
 │    └── AppDemo.java  # Classe principal
 ├── src/test/java/...  # Testes unitários e de integração
 ├── pom.xml            # Configuração Maven
 └── .github/workflows/maven.yml   # Pipeline de CI/CD no GitHub Actions
```

---

## 🎯 Objetivos do Projeto

* Demonstrar a criação de uma aplicação **ERP modularizada**.
* Expor **endpoints REST** para operações de CRUD em entidades como Cliente, Produto e Pedido.
* Aplicar **boas práticas** em camadas de serviço e persistência.
* Usar **testes automatizados** como parte do desenvolvimento.

---

## ⚡ Integração Contínua (CI/CD)

O projeto conta com um workflow configurado no **GitHub Actions** (`.github/workflows/maven.yml`) que executa automaticamente:

1. **Checkout do código**
2. **Compilação e execução dos testes unitários e de integração**
3. **Upload de relatórios de testes** como artefatos no GitHub Actions
4. **Publicação dos artefatos JAR/WAR** para download direto do workflow
5. **Envio do snapshot de dependências** para o **Dependency Graph** do GitHub
6. **Deploy automático no GitHub Packages (Maven Repository)** em caso de push na branch `main`

📍 Configurações de segurança e análise podem ser ajustadas em:
👉 [GitHub Settings - Security Analysis](https://github.com/ramiralvesmelo/app-demo/settings/security_analysis)

---

## 📜 Licença

Este projeto é distribuído sob a licença **MIT**.
Sinta-se livre para usar, modificar e compartilhar.

---

👨‍💻 **Autor:** Ramir Alves
📧 Contato: [ramiralves@gmail.com](mailto:ramiralves@gmail.com)