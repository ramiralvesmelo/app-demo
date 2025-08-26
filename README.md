# 📘 App Demo ERP

[![Build Status](https://github.com/ramiralvesmelo/app-demo/actions/workflows/maven.yml/badge.svg)](https://github.com/ramiralvesmelo/app-demo/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.springframework.boot/spring-boot-starter.svg?logo=apache-maven)](https://search.maven.org/artifact/org.springframework.boot/spring-boot-starter)
[![Java](https://img.shields.io/badge/Java-11-blue.svg?logo=java)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Coverage Status](https://img.shields.io/codecov/c/github/ramiralvesmelo/app-demo?logo=codecov)](https://app.codecov.io/gh/ramiralvesmelo/app-demo)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

📘 **Aplicação de Demonstração ERP**

Projeto em **Spring Boot 2.7** que simula um ERP simplificado, criado como base de estudo para **boas práticas em Java**.

### 🚀 Objetivos

* Demonstrar uma arquitetura em camadas bem definida (**Controller**, **Service**, **Repository**, **Model**).
* Expor APIs **RESTful** com validação de entrada e tratamento centralizado de erros.
* Utilizar **JPA/Hibernate** para persistência e mapeamento objeto-relacional.
* Garantir qualidade através de **testes automatizados** (unitários e de integração) com **JUnit 5** e **Mockito**.
* Implementar **Integração Contínua** com **GitHub Actions**.
* Monitorar **cobertura de código** com **Codecov**.
* Automatizar a **publicação de binários** no **GitHub Packages**.
* Alimentar o **GitHub Dependency Graph** e **Dependabot alerts** via *dependency snapshot* no pipeline (Maven).
* Publicar **relatórios de teste JUnit** (Surefire/Failsafe) como artefatos do CI para inspeção e auditoria.

### 🛠️ Tecnologias

* Spring Boot (Web, Data JPA, Validation)
* Hibernate + **H2** em memória
* Lombok
* JUnit 5 + Mockito
* GitHub Actions + Codecov
* Maven

### 📌 Funcionalidades

* CRUD de **Clientes** e **Pedidos** com itens.
* Cálculo de totais e regras simples (finalizar/cancelar).
* Validações de dados e mensagens de erro customizadas.

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