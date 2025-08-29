# 📘 App Demo ERP

[![Build Status](https://github.com/ramiralvesmelo/app-demo/actions/workflows/maven.yml/badge.svg)](https://github.com/ramiralvesmelo/app-demo/actions/workflows/maven.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.springframework.boot/spring-boot-starter.svg?logo=apache-maven)](https://search.maven.org/artifact/org.springframework.boot/spring-boot-starter)
[![Java](https://img.shields.io/badge/Java-11-blue.svg?logo=java)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![Coverage Status](https://img.shields.io/codecov/c/github/ramiralvesmelo/app-demo?logo=codecov)](https://app.codecov.io/gh/ramiralvesmelo/app-demo)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ramiralvesmelo_app-demo&metric=alert_status)](https://sonarcloud.io/dashboard?id=ramiralvesmelo_app-demo)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=ramiralvesmelo_app-demo&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=ramiralvesmelo_app-demo)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=ramiralvesmelo_app-demo&metric=security_rating)](https://sonarcloud.io/dashboard?id=ramiralvesmelo_app-demo)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=ramiralvesmelo_app-demo&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=ramiralvesmelo_app-demo)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

📘 **Aplicação de Demonstração ERP**

O projeto apresenta um **código-fonte simples e bem estruturado** no modelo arquitetural **MVC**, acompanhado por **testes unitários e de integração**. Essa combinação garante **clareza na arquitetura**, **confiabilidade no código** e oferece uma base sólida para **aprendizado**, **experimentação** e **amadurecimento de ideias**.

Além do código, o repositório integra-se a um conjunto de **ferramentas modernas de DevSecOps**, simulando o cotidiano de um time profissional e demonstrando como **práticas corporativas podem ser aplicadas em projetos Java**.

A proposta do projeto não é apenas apresentar o código-fonte, mas **evidenciar sua interação com o ambiente e com todo o ecossistema de ferramentas**, destacando como cada componente contribui para um **fluxo de desenvolvimento seguro, automatizado e alinhado às demandas do mercado atual**.

### 🎯 Objetivos

* 🗄️ **Utilizar JPA/Hibernate** para persistência e mapeamento objeto-relacional.
* ✅ **Garantir qualidade com testes automatizados** (unitários e de integração) utilizando **JUnit 5** e **Mockito**.
* 🔄 **Implementar Integração Contínua** com **GitHub Actions**.
* 📊 **Integrar e monitorar cobertura de código** com **Codecov**.
* 📦 **Automatizar publicação de binários** no **GitHub Packages**.
* 📈 **Alimentar o GitHub Dependency Graph**.
* 📄 **Gerar e publicar SBOM (Software Bill of Materials)**
* 🚨 **Habilitar Dependabot Alerts** via **dependency snapshot** no pipeline (Maven).
* 📑 **Publicar relatórios de teste JUnit** (Surefire/Failsafe) como artefatos do CI para inspeção e auditoria.
* 🔍 **Analisar o projeto com SonarCloud** para qualidade, segurança e manutenibilidade do código.
* 🏷️ **Exibir badges de status** (build, qualidade, cobertura, dependabot) no README de forma simplificada.

---

### 🛠️ Tecnologias

* **Spring Boot 2.7.18**

  * Spring Web
  * Spring Data JPA
  * Spring Validation
* **Hibernate ORM 5.6.15.Final**
* **H2 Database 2.1.214** (runtime, em memória)
* **Lombok 1.18.32**
* **JUnit 5.9.3** + **Mockito 5.10.0**
* **Maven 3.9.2**
* **GitHub Actions** (CI/CD)
* **Codecov** (monitoramento de cobertura de testes)

---

## ⚙️ Pré-requisitos

* [Java 11+](https://adoptium.net/)
* [Maven 3.8+](https://maven.apache.org/)
* [Lombok plugin](https://projectlombok.org/setup/) instalado na IDE (**Eclipse STS** ou **IntelliJ IDEA**) para suporte às anotações.

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

## ✅ Comandos Maven

Aqui está uma tabela organizada com os **plugins configurados no `pom.xml`**, suas fases e seus respectivos comandos manuais.

---

| 🔌 Plugin                    | ⚡ Fase Maven (default)                         | ▶️ Comando Maven Manual                                      |
| ---------------------------- | ---------------------------------------------- | ------------------------------------------------------------ |
| **maven-resources-plugin**   | `process-resources` / `process-test-resources` | `mvn resources:resources` <br> `mvn resources:testResources` |
| **maven-compiler-plugin**    | `compile` / `test-compile`                     | `mvn compiler:compile` <br> `mvn compiler:testCompile`       |
| **spring-boot-maven-plugin** | `package` (reempacota JAR/WAR)                 | `mvn spring-boot:run` <br> `mvn spring-boot:repackage`       |
| **jacoco-maven-plugin**      | `verify` (gera relatórios)                     | `mvn jacoco:prepare-agent` <br> `mvn jacoco:report`          |
| **maven-surefire-plugin**    | `test`                                         | `mvn test` <br> `mvn surefire:test`                          |
| **cyclonedx-maven-plugin**   | `verify`                                       | `mvn cyclonedx:makeAggregateBom`                             |

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

## 🌐 Collection de Endpoints

O repositório já inclui a collection `app-demo-collection.json`, que contém exemplos prontos de chamadas à API (Clientes, Produtos e Pedidos).

### Importar no Postman ou Insomnia

1. Clique em **Import > File**
2. Selecione `app-demo-collection.json`

---

## 🗂️ Estrutura do Projeto

```
app-demo
├── src/main/java/br/com/springboot/erp
│   ├── controller    # Camada de exposição (REST Controllers)
│   ├── model         # Entidades JPA (entity domínio)
│   ├── repository    # Repositórios JPA (Spring Data)
│   ├── service       # Regras de negócio (@Transactional)
│   └── AppDemo.java  # Classe principal Spring Boot
├── src/test/java/... # Testes unitários e de integração
├── pom.xml           # Build Maven e plugins (Surefire, JaCoCo, CycloneDX)
└── .github/workflows/maven.yml  # Pipeline CI
```

---

## 🔐 Secrets

* **SONAR\_TOKEN** → gerar em **SonarCloud → My Account → Security**
* **CODECOV\_TOKEN** → gerar no **Codecov** (Action v4)
* **GITHUB\_TOKEN** → já fornecido pelo **GitHub Actions**  

### ⚠️ Atenção

O comando `sonar:sonar` **somente funciona no modo CI-based Analysis**.
Se o projeto estiver em **AutoScan**, a execução **irá falhar**.

### ✅ Como configurar a análise via pipeline

1. No **SonarCloud**, acesse:
   **Administration → Analysis Method → selecione CI-based Analysis**.
2. Configure os segredos no repositório.
3. Verifique se o método de análise está definido como **CI-based**.
4. Execute o pipeline com `sonar:sonar`.

---

📍 Configurações de segurança e análise também podem ser ajustadas em:
👉 [GitHub Settings - Security Analysis](https://github.com/ramiralvesmelo/app-demo/settings/security_analysis)

---

## 📜 Licença

Este projeto é distribuído sob a licença **MIT**.
Sinta-se livre para usar, modificar e compartilhar.
