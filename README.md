# Accounting of Student Retakes Server

Серверное приложение для управления пересдачами студентов. Позволяет отслеживать задолженности студентов, управлять записью на пересдачи, контролировать результаты и проводить аналитику.

## Описание проекта

Это RESTful API сервер, разработанный на **Kotlin** с использованием фреймворка **Ktor**, который обеспечивает:
- Аутентификацию и авторизацию на основе JWT
- Управление пользователями (администраторы, учителя, студенты)
- Отслеживание задолженностей студентов
- Управление пересдачами и записью на них
- Комментарии и оценки за пересдачи
- CORS поддержку
- Логирование запросовgit push amvera master

## Технологический стек

| Компонент | Версия |
|-----------|--------|
| **Kotlin** | 2.3.0 |
| **Ktor** | 3.4.0 |
| **Java JVM** | 21 |
| **PostgreSQL** | 42.7.4 |
| **Exposed ORM** | 0.55.0 |
| **HikariCP** | 6.0.0 (пул соединений) |
| **JWT** | 4.4.0 (Auth0) |
| **Bcrypt** | 0.10.2 (хеширование паролей) |
| **OpenAPI/Swagger** | 5.4.0 / 3.3.0 |
| **Logback** | 1.5.6 |

## Архитектура проекта

```
src/main/kotlin/
├── main.kt                        # Точка входа приложения
├── Routing.kt                     # Конфигурация маршрутов
├── controller/                    # HTTP контроллеры
│   ├── AdminController.kt
│   ├── AuthController.kt
│   ├── GuestController.kt
│   ├── StudentController.kt
│   ├── TeacherController.kt
│   └── UserController.kt
├── domain/                        # Бизнес-логика
│   ├── model/                     # Модели данных
│   ├── repository/                # Интерфейсы репозиториев
│   └── usecases/                  # Use-cases (сценарии использования)
├── data/                          # Слой доступа к данным
│   ├── databases/                 # Конфигурация БД
│   ├── dto/                       # Data Transfer Objects
│   ├── mappers/                   # Мапперы моделей
│   └── repository/                # Реализация репозиториев
├── plugins/                       # Плагины Ktor
│   ├── configureAuthentication.kt # JWT аутентификация
│   ├── configureCORS.kt           # CORS конфигурация
│   ├── configureCallLogging.kt    # Логирование запросов
│   ├── configureStatusPages.kt    # Обработка ошибок
│   ├── contentNegotiation.kt      # Content negotiation
│   ├── rolePlugin.kt              # Проверка ролей
│   └── ownStudentPlugin.kt        # Проверка принадлежности студента
├── security/                      # Безопасность
│   ├── JwtConfig.kt               # Конфигурация JWT
│   └── PasswordHasher.kt           # Хеширование паролей
├── helpers/                       # Вспомогательные функции
│   ├── currentEmail.kt
│   ├── fetchTeacherIds.kt
│   ├── longPathParam.kt
│   ├── parseIsoInstant.kt
│   ├── requireOwnStudent.kt
│   ├── requireRole.kt
│   └── validateAndNormalizeRetake.kt
└── dI/                            # Dependency Injection
    └── AppModule.kt
```

## Основные API endpoints

### Аутентификация
- `POST /auth/login` - Вход в систему

### Общие (без аутентификации)
- `GET /general/*` - Общедоступные ресурсы

### Студенты (требуется роль STUDENT)
- `GET /api/student/{studentId}/debts` - Получить список задолженностей
- `GET /api/student/{studentId}/debts/rank` - Получить рейтинг задолженностей
- `GET /api/student/{studentId}/retakes/available` - Доступные пересдачи
- `GET /api/student/{studentId}/retakes/enrolled` - Записанные пересдачи
- `POST /api/student/{studentId}/debts/{debtId}/retakes/{retakeId}` - Записать на пересдачу
- `DELETE /api/student/{studentId}/debts/{debtId}/retakes/{retakeId}` - Отменить запись на пересдачу
- `POST /api/student/{studentId}/comments` - Создать комментарий/оценку

### Учителя (требуется роль TEACHER)
- `GET /api/teacher/*` - Управление пересдачами

### Администраторы (требуется роль ADMIN)
- `GET/POST/PUT/DELETE /api/admin/*` - Управление всеми ресурсами

### Пользователи (все аутентифицированные)
- `GET/POST /api/users/*` - Управление профилем

## Модели данных

### Основные сущности
- **User** - Пользователь (ADMIN, TEACHER, STUDENT)
- **Subject** - Предмет
- **Retake** - Пересдача (экзамен/зачет)
- **StudentDebt** - Задолженность студента
- **RetakeEnrollment** - Запись студента на пересдачу
- **Comment** - Комментарий/оценка за пересдачу
- **Teacher** - Информация об учителе
- **StudentDebtRank** - Рейтинг задолженностей

## Требования к системе

- **Java 21** или выше
- **PostgreSQL 12.0** или выше
- **Docker** (опционально, для запуска в контейнере)
- **Gradle 7.0** или выше

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd AccountingOfStudentRetakesServer
```

### 2. Конфигурация БД

Отредактируйте файл `src/main/resources/application.yaml` и укажите параметры PostgreSQL:

```yaml
database:
  url: jdbc:postgresql://localhost:5432/retakes_db
  driver: org.postgresql.Driver
  user: postgres
  password: your_password
```

### 3. Запуск

#### Локальный запуск

```bash
./gradlew run
```

Сервер запустится на `http://0.0.0.0:8080`

#### Через Docker

```bash
# Сборка Docker образа
./gradlew buildImage

# Запуск с docker-compose
docker-compose up
```

## Команды сборки и тестирования

| Команда | Описание |
|---------|----|
| `./gradlew run` | Запустить сервер локально |
| `./gradlew build` | Собрать проект |
| `./gradlew test` | Запустить тесты |
| `./gradlew buildFatJar` | Собрать исполняемый JAR со всеми зависимостями |
| `./gradlew buildImage` | Собрать Docker образ |
| `./gradlew runDocker` | Запустить сервер в Docker контейнере |

## Успешный запуск

Если сервер стартовал успешно, вы увидите в логах:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

## Аутентификация

Приложение использует **JWT (JSON Web Tokens)** для аутентификации.

### Процесс авторизации:
1. Отправить POST запрос на `/auth/login` с email и password
2. Получить JWT токен
3. Включить токен в заголовок `Authorization: Bearer <token>` для всех защищенных запросов

### Роли пользователей:
- **ADMIN** - Полный доступ ко всем ресурсам
- **TEACHER** - Управление пересдачами и оценками студентов
- **STUDENT** - Просмотр собственных задолженностей и запись на пересдачи

## Логирование

Приложение использует **Logback** для логирования. Конфигурация находится в `src/main/resources/logback.xml`.

Логируются:
- HTTP запросы и ответы
- Ошибки приложения
- SQL запросы (в режиме debug)

## Переменные окружения

Проект поддерживает следующие переменные окружения:

```
DB_URL=jdbc:postgresql://localhost:5432/retakes_db
DB_USER=postgres
DB_PASSWORD=password
JWT_SECRET=your-secret-key
JWT_ISSUER=your-issuer
JWT_AUDIENCE=your-audience
```
## Полезные ссылки

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
- [Exposed ORM Guide](https://github.com/jetbrains/exposed)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT.io](https://jwt.io/)