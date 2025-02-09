# Spring Boot Authentication Template

## Descrição
Este projeto é um template de autenticação para APIs REST desenvolvidas com Spring Boot. O template inclui:

- CRUD de usuário;
- Validação de e-mail ao criar conta;
- Recuperação de senha;
- Autenticação de dois fatores (2FA).

## Requisitos

- Java 17 ou superior;
- Spring Boot 3.0 ou superior;
- Banco de dados PostgreSQL;
- Maven ou Gradle.

## Instalação

1. Clone este repositório:
   ```bash
   git clone https://github.com/iZezito/spring-auth.git
   ```
2. Configure o arquivo `application.properties` com as credenciais do seu banco de dados:
   ```properties
    spring.application.name=sua_aplicacao
    spring.datasource.url=jdbc:postgresql://localhost:5432/sua_base_de_dados
    spring.datasource.username=seu_usuario
    spring.datasource.password=sua_senha
    spring.datasource.driver-class-name=org.postgresql.Driver
    
    spring.jpa.generate-ddl=true
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.properties.hibernate.temp.use_jdbc_metadata_defaults=false
    spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
    
   ```
   > **Nota:** Considere usar variáveis de ambiente para configurar as credenciais do banco de dados para melhorar a segurança.

3. Configure o secret do token JWT `application.properties`:
   ```properties
   springauth.security.token.secret=${JWT_SECRET:12345678}
   ```
   > **Nota:** Substitua os valores acima pelas suas configurações reais. Considere usar variáveis de ambiente para as credenciais sensíveis.

4. Configure o serviço de e-mail no arquivo `application.properties`:
   ```properties
   spring.mail.host=****
   spring.mail.port=587
   spring.mail.username=****
   spring.mail.password=****
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```
   > **Nota:** Substitua os valores acima pelas suas configurações reais. Considere usar variáveis de ambiente para as credenciais sensíveis.


5. Execute a aplicação:
   ```bash
   mvn spring-boot:run
   ```

## Endpoints

### 1. Registro de Usuário

**Descrição:** Registra um novo usuário e envia um e-mail de confirmação.

- **URL:** `/usuarios`
- **Método:** `POST`
- **Corpo da Requisição:**

  ```json
  {
    "nome": "John Doe",
    "email": "john.doe@example.com",
    "password": "senha123"
  }
  ```

- **Resposta de Sucesso:**

  ```json
  {
    "message": "Usuário registrado. Verifique seu e-mail para ativação."
  }
  ```

### 2. Confirmação de E-mail

**Descrição:** Confirma o e-mail do usuário utilizando um token enviado.

- **URL:** `/usuarios/verify-email`
- **Método:** `GET`
- **Parâmetros de Consulta:**

    - `token`: Token de confirmação enviado por e-mail.

- **Exemplo de Requisição:**

  ```
  GET /usuarios/verify-email?token=abc123xyz
  ```

- **Resposta de Sucesso:**

  ```json
  {
    "message": "E-mail verificado com sucesso!"
  }
  ```

### 3. Recuperação de Senha

**Descrição:** Envia um e-mail com o link para redefinição de senha.

- **URL:** `/usuarios/password-reset-tk`
- **Método:** `POST`
- **Parâmetros de Consulta:**

    - `email`: email do usuário

**Exemplo de Requisição:**

  ```
  POST /usuarios/password-reset-tk?email=example@gmail.com
  ```

- **Resposta de Sucesso:**

  ```json
  {
    "message": "E-mail de recuperação de senha enviado."
  }
  ```

### 4. Redefinição de Senha

**Descrição:** Redefine a senha do usuário utilizando um token enviado por e-mail.

- **URL:** `/usuarios/password-reset`
- **Método:** `POST`
- **Parâmetros de Consulta:**

    - `token`: token recebido no email
    - `newPassword`: nova senha para o usuário

**Exemplo de Requisição:**

  ```
  POST /usuarios/password-reset?token=783259cf-44d4-4dff-8a3e-f45a89dd2cba&newPassword=1111111
  ```

- **Resposta de Sucesso:**

  ```json
  {
    "message": "Senha alterada com sucesso."
  }
  ```

### 5. Login

**Descrição:** Autentica o usuário e retorna um token JWT ou solicita autenticação 2FA, se habilitado.

- **URL:** `/login`

- **Método:** `POST`

- **Corpo da Requisição:**

  ```json
  {
    "login": "john.doe@example.com",
    "senha": "senha123",
    "codigo": "123456" // Opcional, necessário apenas se 2FA estiver habilitado
  }
  ```

- **Resposta de Sucesso (Autenticação com 2FA habilitada e código ausente):**

  ```json
  {
    "message": "Código de autenticação enviado para o e-mail."
  }
  ```

- **Resposta de Sucesso (Autenticação com 2FA validada ou sem 2FA):**

  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "nome": "John Doe"
  }
  ```

- **Resposta de Erro (Email não validado):**

  ```json
  {
    "message": "E-mail não validado, cheque sua caixa de entrada!"
  }
  ```

- **Resposta de Erro (Credenciais inválidas):**

  ```json
  {
    "message": "Credenciais inválidas. Por favor, verifique seu e-mail e senha e tente novamente."
  }
  ```

- **Resposta de Erro (Código 2FA inválido ou expirado):**

  ```json
  {
    "message": "Código 2FA inválido ou expirado."
  }
  ```

## Estrutura da Entidade `Usuario`

```java
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String password;
    private boolean emailVerified;
    private boolean twoFactorAuthenticationEnabled;
    // Getters, Setters e outros métodos omitidos
}
```

## Tecnologias Utilizadas

- Spring Boot 3.0;
- Spring Security;
- Spring Boot DevTools;
- Lombok;
- Spring Web;
- JDBC API;
- Spring Data JPA;
- PostgreSQL Driver;
- Validation;
- Java Mail Sender;
- JWT.

## Licença
Este projeto está licenciado sob a [Licença Emersoniana](LICENSE).
