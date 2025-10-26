# Authentication Service

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

[English](#english) | [Українська](#ukrainian)

---

## English

### 📋 Overview

The Authentication Service is a core microservice of the Gym Management System that handles user authentication, authorization, and account management. It provides secure JWT-based authentication with email verification, password reset functionality, and comprehensive security features including brute force protection and rate limiting.

### 🚀 Features

- **User Registration & Email Verification**
    - Secure user registration with validation
    - 6-digit email verification code (15-minute expiry)
    - Automatic email sending via Kafka events
    - Rate limiting (3 attempts per hour)

- **Authentication & Authorization**
    - JWT-based authentication (RS256 algorithm)
    - Access tokens (24-hour expiry)
    - Refresh tokens (7-day expiry)
    - Role-based access control (ADMIN, TRAINER, MEMBER)

- **Password Management**
    - Secure password reset flow
    - Email-based reset tokens (1-hour expiry)
    - Password strength validation
    - BCrypt password hashing

- **Security Features**
    - Brute force protection (5 attempts per 15 minutes)
    - Token blacklisting on logout
    - IP-based rate limiting
    - Redis-backed session management

- **Integration & Monitoring**
    - Spring Cloud Config integration
    - Eureka service discovery
    - OpenAPI/Swagger documentation
    - Distributed tracing with Zipkin
    - Kafka event publishing

### 🛠️ Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming Language |
| Spring Boot | 3.5.7 | Application Framework |
| Spring Security | 6.x | Security Framework |
| Spring Data JPA | 3.x | Data Access |
| PostgreSQL | Latest | Primary Database |
| Redis | Latest | Caching & Session Management |
| Kafka | Latest | Event Streaming |
| JWT (JJWT) | 0.13.0 | Token Management |
| Lombok | 1.18.42 | Code Generation |
| SpringDoc OpenAPI | 2.8.11 | API Documentation |
| Caffeine | Latest | In-Memory Caching |

### 📁 Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/.../auth_service/
│   │   │   ├── config/              # Configuration classes
│   │   │   │   ├── BeansConfig.java
│   │   │   │   ├── KafkaConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/          # REST controllers
│   │   │   │   └── AuthenticationController.java
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   │   ├── kafka/           # Kafka event DTOs
│   │   │   │   ├── request/         # Request DTOs
│   │   │   │   └── response/        # Response DTOs
│   │   │   ├── entity/              # JPA entities
│   │   │   │   ├── BaseEntity.java
│   │   │   │   ├── EmailVerification.java
│   │   │   │   ├── PasswordResetToken.java
│   │   │   │   ├── Role.java
│   │   │   │   └── UserCredentials.java
│   │   │   ├── exception/           # Exception handling
│   │   │   ├── mapper/              # Entity-DTO mappers
│   │   │   ├── repository/          # Data repositories
│   │   │   ├── security/            # Security components
│   │   │   │   ├── filters/         # Security filters
│   │   │   │   ├── JwtFilter.java
│   │   │   │   └── RefreshTokenFilter.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── impl/            # Service implementations
│   │   │   │   └── interfaces
│   │   │   └── utils/               # Utility classes
│   │   └── resources/
│   │       ├── keys/local-only/     # RSA keys for JWT
│   │       │   ├── private_key.pem
│   │       │   └── public_key.pem
│   │       └── application.yml
│   └── test/                        # Test classes
├── pom.xml
└── README.md
```

### ⚙️ Configuration

#### Prerequisites

- Java 21 or higher
- Maven 3.9+
- PostgreSQL database
- Redis server
- Kafka broker
- Config Server (running on port 8888)
- Eureka Server (running on port 8761)

#### Application Properties

The service uses Spring Cloud Config for externalized configuration. Key configurations:

```yaml
server:
  port: 8090

spring:
  datasource:
    url: jdbc:postgresql://host:5432/auth_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  redis:
    host: localhost
    port: 6380
  
  kafka:
    bootstrap-servers: localhost:9092

app:
  security:
    jwt:
      access-token-expiration: 86400000    # 24 hours
      refresh-token-expiration: 604800000  # 7 days
```

### 🚦 Getting Started

#### 1. Clone the repository

```bash
git clone https://github.com/your-org/gym-management-system.git
cd gym-management-system/auth-service
```

#### 2. Configure environment variables

Create `.env` file or set environment variables:

```bash
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export REDIS_HOST=localhost
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

#### 3. Generate RSA Keys (if needed)

```bash
# Generate private key
openssl genrsa -out src/main/resources/keys/local-only/private_key.pem 2048

# Generate public key
openssl rsa -in src/main/resources/keys/local-only/private_key.pem \
  -pubout -out src/main/resources/keys/local-only/public_key.pem
```

#### 4. Start dependencies

```bash
# Using Docker Compose (from project root)
cd ../docker
docker-compose up -d postgres redis kafka zookeeper
```

#### 5. Run the application

```bash
# Using Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

### 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8090/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8090/v3/api-docs

#### Key Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/register` | Register new user | No |
| POST | `/api/v1/auth/login` | User login | No |
| POST | `/api/v1/auth/verify-email` | Verify email address | No |
| POST | `/api/v1/auth/refresh-token` | Refresh access token | Yes (Refresh) |
| POST | `/api/v1/auth/logout` | User logout | Yes |
| POST | `/api/v1/auth/forgot-password` | Request password reset | No |
| POST | `/api/v1/auth/reset-password` | Reset password | No |
| GET | `/api/v1/auth/validate-token` | Validate JWT token | Yes |

### 🔐 Security

#### Password Requirements

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character

#### Rate Limits

- **Registration**: 3 attempts per hour per IP
- **Login**: 5 attempts per 15 minutes per IP
- **Email Verification**: 5 attempts per code
- **Resend Verification**: 3 per hour per email

#### JWT Token Structure

```json
{
  "sub": "user@example.com",
  "userId": "uuid",
  "roles": ["ROLE_MEMBER"],
  "token_type": "ACCESS_TOKEN",
  "iat": 1234567890,
  "exp": 1234654290
}
```

### 📊 Database Schema

#### Core Tables

- **user_credentials**: User account information
- **roles**: System roles (ADMIN, TRAINER, MEMBER)
- **user_roles**: User-role associations
- **email_verification**: Email verification codes
- **password_reset_tokens**: Password reset tokens

### 🔄 Event Publishing

The service publishes events to Kafka topics:

| Topic | Event | Trigger |
|-------|-------|---------|
| `code-topic` | CodeConfirmation | User registration |
| `user-registered-topic` | UserRegisteredEvent | Successful registration |
| `user-login-topic` | UserLoginEvent | Successful login |
| `password-reset-topic` | PasswordResetEvent | Password reset request |

### 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify -P integration-tests
```

### 📈 Monitoring & Health Checks

- **Health Check**: http://localhost:8090/actuator/health
- **Metrics**: http://localhost:8090/actuator/metrics
- **Info**: http://localhost:8090/actuator/info

### 🐛 Troubleshooting

#### Common Issues

**Problem**: Cannot connect to database
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Verify connection
psql -h localhost -p 5432 -U postgres -d auth_db
```

**Problem**: Redis connection failed
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli -p 6380 ping
```

**Problem**: Kafka not available
```bash
# Check Kafka and Zookeeper
docker ps | grep kafka
docker ps | grep zookeeper

# Test Kafka connection
kafka-topics.sh --list --bootstrap-server localhost:9092
```

### 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### 📞 Support

- **Email**: support@quikkkk.dev

---

## Ukrainian

### 📋 Огляд

Сервіс автентифікації є основним мікросервісом системи управління тренажерним залом, який обробляє автентифікацію користувачів, авторизацію та управління обліковими записами. Він забезпечує безпечну автентифікацію на основі JWT з підтвердженням електронної пошти, функціональністю скидання пароля та комплексними функціями безпеки, включаючи захист від брутфорсу та обмеження частоти запитів.

### 🚀 Функціональність

- **Реєстрація користувачів та підтвердження email**
    - Безпечна реєстрація з валідацією
    - 6-значний код підтвердження (дійсний 15 хвилин)
    - Автоматична відправка листів через Kafka
    - Обмеження частоти запитів (3 спроби на годину)

- **Автентифікація та авторизація**
    - JWT-автентифікація (алгоритм RS256)
    - Access токени (дійсні 24 години)
    - Refresh токени (дійсні 7 днів)
    - Контроль доступу на основі ролей (ADMIN, TRAINER, MEMBER)

- **Управління паролями**
    - Безпечний процес скидання пароля
    - Токени скидання через email (дійсні 1 годину)
    - Валідація складності пароля
    - Хешування паролів BCrypt

- **Функції безпеки**
    - Захист від брутфорсу (5 спроб за 15 хвилин)
    - Чорний список токенів при виході
    - Обмеження на основі IP-адреси
    - Управління сесіями через Redis

- **Інтеграція та моніторинг**
    - Інтеграція з Spring Cloud Config
    - Service discovery через Eureka
    - Документація OpenAPI/Swagger
    - Розподілене трасування з Zipkin
    - Публікація подій у Kafka

### 🛠️ Технологічний стек

| Технологія | Версія | Призначення |
|------------|--------|-------------|
| Java | 21 | Мова програмування |
| Spring Boot | 3.5.7 | Фреймворк додатку |
| Spring Security | 6.x | Фреймворк безпеки |
| Spring Data JPA | 3.x | Доступ до даних |
| PostgreSQL | Latest | Основна БД |
| Redis | Latest | Кешування та сесії |
| Kafka | Latest | Потокова обробка подій |
| JWT (JJWT) | 0.13.0 | Управління токенами |
| Lombok | 1.18.42 | Генерація коду |
| SpringDoc OpenAPI | 2.8.11 | Документація API |
| Caffeine | Latest | Кешування в пам'яті |

### 📁 Структура проєкту

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/.../auth_service/
│   │   │   ├── config/              # Класи конфігурації
│   │   │   ├── controller/          # REST контролери
│   │   │   ├── dto/                 # Об'єкти передачі даних
│   │   │   ├── entity/              # JPA сутності
│   │   │   ├── exception/           # Обробка винятків
│   │   │   ├── mapper/              # Маппери Entity-DTO
│   │   │   ├── repository/          # Репозиторії даних
│   │   │   ├── security/            # Компоненти безпеки
│   │   │   ├── service/             # Бізнес-логіка
│   │   │   └── utils/               # Утилітні класи
│   │   └── resources/
│   │       ├── keys/local-only/     # RSA ключі для JWT
│   │       └── application.yml
│   └── test/                        # Тестові класи
├── pom.xml
└── README.md
```

### ⚙️ Конфігурація

#### Передумови

- Java 21 або вище
- Maven 3.9+
- База даних PostgreSQL
- Redis сервер
- Kafka брокер
- Config Server (порт 8888)
- Eureka Server (порт 8761)

### 🚦 Початок роботи

#### 1. Клонування репозиторію

```bash
git clone https://github.com/your-org/gym-management-system.git
cd gym-management-system/auth-service
```

#### 2. Налаштування змінних середовища

```bash
export DB_USERNAME=ваш_користувач_бд
export DB_PASSWORD=ваш_пароль_бд
export REDIS_HOST=localhost
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

#### 3. Генерація RSA ключів (за необхідності)

```bash
# Генерація приватного ключа
openssl genrsa -out src/main/resources/keys/local-only/private_key.pem 2048

# Генерація публічного ключа
openssl rsa -in src/main/resources/keys/local-only/private_key.pem \
  -pubout -out src/main/resources/keys/local-only/public_key.pem
```

#### 4. Запуск залежностей

```bash
# Використовуючи Docker Compose
cd ../docker
docker-compose up -d postgres redis kafka zookeeper
```

#### 5. Запуск додатку

```bash
# Через Maven
./mvnw spring-boot:run

# Або збірка та запуск JAR
./mvnw clean package
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

### 📚 Документація API

Після запуску додатку, доступна інтерактивна документація:

- **Swagger UI**: http://localhost:8090/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8090/v3/api-docs

#### Основні ендпоінти

| Метод | Ендпоінт | Опис | Потрібна авторизація |
|-------|----------|------|---------------------|
| POST | `/api/v1/auth/register` | Реєстрація користувача | Ні |
| POST | `/api/v1/auth/login` | Вхід в систему | Ні |
| POST | `/api/v1/auth/verify-email` | Підтвердження email | Ні |
| POST | `/api/v1/auth/refresh-token` | Оновлення токена | Так (Refresh) |
| POST | `/api/v1/auth/logout` | Вихід | Так |
| POST | `/api/v1/auth/forgot-password` | Запит скидання пароля | Ні |
| POST | `/api/v1/auth/reset-password` | Скидання пароля | Ні |
| GET | `/api/v1/auth/validate-token` | Валідація JWT токена | Так |

### 🔐 Безпека

#### Вимоги до пароля

- Мінімум 8 символів
- Хоча б одна велика літера
- Хоча б одна мала літера
- Хоча б одна цифра
- Хоча б один спеціальний символ

#### Обмеження частоти запитів

- **Реєстрація**: 3 спроби на годину на IP
- **Вхід**: 5 спроб за 15 хвилин на IP
- **Підтвердження email**: 5 спроб на код
- **Повторна відправка коду**: 3 на годину на email

### 🧪 Тестування

```bash
# Запуск всіх тестів
./mvnw test

# Запуск з покриттям
./mvnw test jacoco:report

# Інтеграційні тести
./mvnw verify -P integration-tests
```

### 📈 Моніторинг та перевірки

- **Health Check**: http://localhost:8090/actuator/health
- **Метрики**: http://localhost:8090/actuator/metrics
- **Інформація**: http://localhost:8090/actuator/info

### 🐛 Усунення несправностей

#### Поширені проблеми

**Проблема**: Не можу підключитися до бази даних
```bash
# Перевірка роботи PostgreSQL
docker ps | grep postgres

# Перевірка з'єднання
psql -h localhost -p 5432 -U postgres -d auth_db
```

**Проблема**: Помилка з'єднання з Redis
```bash
# Перевірка роботи Redis
docker ps | grep redis

# Тест з'єднання
redis-cli -p 6380 ping
```

**Проблема**: Kafka недоступна
```bash
# Перевірка Kafka та Zookeeper
docker ps | grep kafka
docker ps | grep zookeeper
```

### 📄 Ліцензія

Цей проєкт ліцензовано під ліцензією MIT - деталі в файлі [LICENSE](LICENSE).

### 📞 Підтримка

- **Email**: support@quikkkk.dev

---