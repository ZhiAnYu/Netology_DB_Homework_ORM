# Отчет по выполнению задачи: Слой DAO с использованием Hibernate

## 1. Цель работы
Реализовать слой DAO (Data Access Object) для Spring Boot приложения с использованием Hibernate через Spring Data JPA. Адаптировать таблицу `PERSONS` из задачи «Таблица пользователей» под ORM-модель с составным первичным ключом (`name`, `surname`, `age`), создать репозиторий на основе нативного API `EntityManager` и REST-контроллер для поиска пользователей по городу проживания.

## 2. Использованные технологии и инструменты

- **ОС:** Windows 10/11
- **Среда разработки:** IntelliJ IDEA Community Edition 2025
- **Язык программирования:** Java 17 (OpenJDK 17.0.19)
- **Сборщик проектов:** Apache Maven
- **Фреймворк:** Spring Boot 3.5.14
  - `spring-boot-starter-web` — для REST API и Tomcat 10.1.54
  - `spring-boot-starter-data-jpa` — для работы с Hibernate 6.6.49.Final
- **База данных:** PostgreSQL 16.14
- **Драйвер БД:** `org.postgresql:postgresql` (версия 42.7.10)
- **Пул соединений:** HikariCP 6.3.3
- **Инструменты управления БД:** DBeaver / pgAdmin (для просмотра схемы и данных)
- **Инструменты тестирования API:** браузер / Postman / `curl`
- **Вспомогательные инструменты:** Qwen (AI-ассистент для генерации boilerplate-кода, отладки синтаксиса SQL/JPQL и диагностики конфигурации Spring Boot)

## 3. Выполненные шаги

### 3.1. Создание Spring Boot приложения
Создан новый Maven-проект со следующими зависимостями в `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### 3.2. Настройка `application.properties`
Обновлён файл конфигурации для подключения к PostgreSQL и настройки Hibernate:

```properties
spring.application.name=HomeworkORM
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

Ключевой момент: параметр `spring.jpa.defer-datasource-initialization=true` заставляет Spring Boot выполнять `data.sql` **после** того, как Hibernate создаст таблицы.

### 3.3. Создание Entity с составным первичным ключом
Поскольку первичный ключ таблицы `PERSONS` состоит из трёх полей (`name`, `surname`, `age`), реализован паттерн `@EmbeddedId`.

**Класс составного ключа `PersonId.java`:**

```java
@Embeddable
public class PersonId implements Serializable {
    private String name;
    private String surname;
    private Integer age;
    
    // equals() и hashCode() обязательны для составных ключей
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
}
```

**Основная сущность `Person.java`:**

```java
@Entity
@Table(name = "PERSONS")
public class Person {
    @EmbeddedId
    private PersonId id;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "city_of_living")
    private String cityOfLiving;
}
```

### 3.4. Реализация DAO-слоя с EntityManager
Создан класс `PersonDao` с аннотацией `@Repository`, в который через `@PersistenceContext` инжектирован `EntityManager`. Реализован метод `getPersonsByCity(String city)` с использованием JPQL:

```java
@Repository
public class PersonDao {
    @PersistenceContext
    private EntityManager entityManager;

    public List<Person> getPersonsByCity(String city) {
        String jpql = "SELECT p FROM Person p WHERE p.cityOfLiving = :city";
        return entityManager.createQuery(jpql, Person.class)
                .setParameter("city", city)
                .getResultList();
    }
}
```

### 3.5. Создание REST-контроллера
Реализован контроллер `PersonController` с endpoint `/persons/by-city`, принимающим query-параметр `city`:

```java
@RestController
@RequestMapping("/persons")
public class PersonController {
    private final PersonDao personDao;

    public PersonController(PersonDao personDao) {
        this.personDao = personDao;
    }

    @GetMapping("/by-city")
    public List<Person> getPersonsByCity(@RequestParam String city) {
        return personDao.getPersonsByCity(city);
    }
}
```

### 3.6. Наполнение БД тестовыми данными
Создан файл `src/main/resources/data.sql` с тестовыми записями для проверки работы API:

```sql
DELETE FROM PERSONS;

INSERT INTO PERSONS (name, surname, age, phone_number, city_of_living) VALUES
('Ivan', 'Ivanov', 25, '+79001112233', 'Moscow'),
('Petr', 'Petrov', 30, '+79004445566', 'Moscow'),
('Dmitry', 'Smirnov', 35, '+79001234567', 'Moscow'),
('Anna', 'Sidorova', 28, '+79007778899', 'Saint Petersburg'),
('Elena', 'Kuznetsova', 22, '+79009876543', 'Kazan'),
('Sergey', 'Popov', 29, '+79005554433', 'Saint Petersburg'),
('Olga', 'Volkova', 32, '+79009998877', 'Moscow');
```

### 3.7. Сохранение SQL-скриптов из прошлой задачи
В отдельный файл `queries.sql` вынесены SQL-запросы из условия задачи «Таблица пользователей»:

```sql
-- Поиск name и surname пользователей из MOSCOW
SELECT name, surname FROM PERSONS WHERE city_of_living = 'MOSCOW';

-- Поиск всех полей, где age > 27, с сортировкой по убыванию
SELECT * FROM PERSONS WHERE age > 27 ORDER BY age DESC;
```

## 4. Проблемы и их решения

### 4.1. Ошибка `syntax error at or near "-"` при запуске `data.sql` (ключевая проблема)

**Симптом:** При старте приложение падало с ошибкой:
```
ScriptStatementFailedException: Failed to execute SQL script statement #1 
of file [...\data.sql]: - Очистка таблицы перед вставкой...
Caused by: org.postgresql.util.PSQLException: ERROR: syntax error at or near "-"
Position: 1
```

**Причина:** Парсер SQL-скриптов Spring Boot (`ScriptUtils`) некорректно обрабатывал SQL-комментарии вида `-- текст` в самом начале файла, воспринимая символы `--` как часть SQL-команды.

**Решение:** 
1. Удалены все комментарии из файла `data.sql` — оставлен только чистый SQL-код.
2. Команда `TRUNCATE TABLE PERSONS RESTART IDENTITY` заменена на более безопасную `DELETE FROM PERSONS`.

### 4.2. Ошибка `table "persons" does not exist`

**Симптом:** `data.sql` выполнялся до того, как Hibernate создавал таблицу.

**Причина:** По умолчанию в Spring Boot 3.x скрипты инициализации выполняются **до** инициализации JPA-провайдера.

**Решение:** Добавлен параметр в `application.properties`:
```properties
spring.jpa.defer-datasource-initialization=true
```
Это заставило Spring отложить выполнение `data.sql` до завершения работы Hibernate.

### 4.3. Предупреждение `PostgreSQLDialect does not need to be specified explicitly`

**Симптом:** В логах Hibernate появлялся WARN:
```
HHH90000025: PostgreSQLDialect does not need to be specified explicitly
```

**Причина:** В Hibernate 6.x диалект определяется автоматически по JDBC-драйверу.

**Решение:** Удалена строка `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect` из `application.properties`.

## 5. Чек-лист самопроверки

- ✅ Создано Spring Boot приложение с зависимостями `spring-boot-starter-web` и `spring-boot-starter-data-jpa`
- ✅ Добавлена зависимость `org.postgresql:postgresql`
- ✅ Создан класс `PersonId` с аннотацией `@Embeddable` для составного первичного ключа
- ✅ Реализованы методы `equals()` и `hashCode()` в классе ключа
- ✅ Создана Entity `Person` с аннотацией `@EmbeddedId`
- ✅ Создан класс `PersonDao` с аннотацией `@Repository`
- ✅ В `PersonDao` инжектирован `EntityManager` через `@PersistenceContext`
- ✅ Реализован метод `getPersonsByCity(String city)` с использованием JPQL
- ✅ Создан контроллер `PersonController` с `@GetMapping("/by-city")`
- ✅ Контроллер принимает query-параметр `city` через `@RequestParam`
- ✅ Настроено подключение к PostgreSQL в `application.properties`
- ✅ Создан файл `data.sql` с тестовыми данными
- ✅ Приложение успешно запускается без ошибок
- ✅ API возвращает корректные JSON-ответы
- ✅ Код выложен в отдельный репозиторий на GitHub

## 6. Полученные результаты

### Работающий REST API:
- `GET /persons/by-city?city=Moscow` → список из 4 пользователей (Ivan, Petr, Dmitry, Olga) — HTTP 200
- `GET /persons/by-city?city=Kazan` → список из 1 пользователя (Elena) — HTTP 200
- `GET /persons/by-city?city=Unknown` → пустой массив `[]` — HTTP 200

### Пример JSON-ответа:
```json
[
  {
    "id": {
      "name": "Ivan",
      "surname": "Ivanov",
      "age": 25
    },
    "phoneNumber": "+79001112233",
    "cityOfLiving": "Moscow"
  },
  {
    "id": {
      "name": "Petr",
      "surname": "Petrov",
      "age": 30
    },
    "phoneNumber": "+79004445566",
    "cityOfLiving": "Moscow"
  }
]
```

### Архитектурные особенности реализации:
- **Составной первичный ключ:** корректно реализован через `@EmbeddedId` с обязательной реализацией `equals()`/`hashCode()` — это требование спецификации JPA для корректной работы кэша первого уровня (Persistence Context).
- **Нативный API Hibernate:** DAO-слой реализован через `EntityManager`, а не через Spring Data JPA интерфейсы, что позволяет глубже понять работу ORM.
- **JPQL вместо SQL:** запросы написаны на Java Persistence Query Language, что обеспечивает независимость от конкретной СУБД.
- **Инъекция через конструктор:** в контроллере использована constructor injection — рекомендуемый способ в Spring для иммутабельных зависимостей.
- **Разделение ответственности:** Entity, DAO и Controller находятся в отдельных пакетах, что соответствует принципам чистой архитектуры.

## 7. Вывод

В ходе выполнения задачи «Слой DAO с Hibernate» были освоены ключевые практики работы с ORM в Spring Boot:

1. **Работа с составными первичными ключами:** реализован паттерн `@EmbeddedId` с классом-ключом `@Embeddable`, включая обязательные `equals()` и `hashCode()`.
2. **Нативный API Hibernate:** освоена работа с `EntityManager` через `@PersistenceContext` и написание JPQL-запросов с именованными параметрами.
3. **Построение REST API:** создан контроллер с `@RestController`, `@GetMapping` и обработкой query-параметров через `@RequestParam`.
4. **Инициализация БД:** настроен механизм выполнения `data.sql` после создания схемы Hibernate через `spring.jpa.defer-datasource-initialization`.
5. **Диагностика и отладка:** решены проблемы с парсингом SQL-комментариев и порядком инициализации контекста Spring.

Реализованное решение полностью соответствует требованиям задания:
- ✅ Использованы стартеры `spring-boot-starter-data-jpa` и `spring-boot-starter-web`
- ✅ Entity соответствует таблице из задачи «Таблица пользователей»
- ✅ Репозиторий реализован через `@Repository` и `EntityManager`
- ✅ Метод `getPersonsByCity(String city)` возвращает отфильтрованные записи
- ✅ Контроллер обрабатывает GET-запросы на `/persons/by-city?city=...`
- ✅ Используется PostgreSQL в качестве СУБД

Приложение готово к использованию и демонстрирует профессиональный подход к построению слоя доступа к данным в Spring Boot приложениях. Код выложен в репозиторий GitHub и готов к проверке.
