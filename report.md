# Отчет по выполнению задачи: JPA репозитории и JPQL

## 1. Цель работы
Рефакторинг приложения «Слой DAO с Hibernate» с переходом от нативного использования `EntityManager` к декларативным Spring Data JPA Repositories. Реализация методов-запросов (Query Derivation) для CRUD-операций, фильтрации и сортировки. Интеграция Liquibase для версионирования схемы БД и добавление `docker-compose.yml` для контейнеризации СУБД.

## 2. Использованные технологии и инструменты
- **ОС:** Windows 10 (WSL2)
- **Среда разработки:** IntelliJ IDEA Community Edition 2025
- **Язык программирования:** Java 17 (OpenJDK 17.0.19)
- **Сборщик проектов:** Apache Maven
- **Фреймворк:** Spring Boot 3.5.14
  - `spring-boot-starter-web` — для REST API
  - `spring-boot-starter-data-jpa` — для Spring Data JPA и Hibernate 6.6.49.Final
- **Система миграций БД:** Liquibase (через `liquibase-core`)
- **База данных:** PostgreSQL 16 (запущена в Docker-контейнере)
- **Драйвер БД:** `org.postgresql:postgresql`
- **Контейнеризация:** Docker Engine / Docker Compose
- **Инструменты тестирования API:** Postman / `curl`
- **Вспомогательные инструменты:** Qwen (AI-ассистент для рефакторинга кода, генерации SQL-миграций и структурирования отчета)

## 3. Выполненные шаги

### 3.1. Создание feature-ветки
Вся дальнейшая разработка велась в изолированной ветке.

### 3.2. Добавление зависимостей
В `pom.xml` проверено наличие зависимостей `spring-boot-starter-data-jpa` и `liquibase-core`. Версии управляются через `spring-boot-starter-parent`.

### 3.3. Контейнеризация СУБД
Создан файл `docker-compose.yml` в корне проекта для быстрого развертывания PostgreSQL 16 с пробросом порта 5432 и сохранением данных в volume.

### 3.4. Настройка `application.properties`
Обновлён файл конфигурации:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.sql.init.mode=never
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
spring.jpa.hibernate.ddl-auto=validate
```

### 3.5. Создание структуры миграций Liquibase
Создана структура директорий и файлы миграций:
- `db/changelog/db.changelog-master.yaml` (главный файл, включающий изменения)
- `db/changelog/changes/v1.0.0-create-persons-table.sql` (создание таблицы `PERSONS` с составным ключом и блоком `--rollback`)
- `db/changelog/changes/v1.0.1-insert-test-data.sql` (наполнение тестовыми данными с блоком `--rollback`)

### 3.6. Рефакторинг слоя доступа к данным (Repository)
Удален кастомный класс `PersonDao`. Создан интерфейс `PersonRepository`, расширяющий `JpaRepository<Person, PersonId>`.
Поскольку первичный ключ является составным (`@EmbeddedId`), для корректного обращения к вложенным полям (`name`, `surname`, `age`) использована аннотация `@Query` с явным указанием JPQL-путей:

```java
@Repository
public interface PersonRepository extends JpaRepository<Person, PersonId> {
  List<Person> findByCityOfLiving(String city);

  @Query("SELECT p FROM Person p WHERE p.id.age < :age ORDER BY p.id.age ASC")
  List<Person> findByAgeLessThanOrderByAgeAsc(@Param("age") Integer age);

  @Query("SELECT p FROM Person p WHERE p.id.name = :name AND p.id.surname = :surname")
  Optional<Person> findByNameAndSurname(@Param("name") String name, @Param("surname") String surname);
}
```
Spring Data JPA автоматически генерирует JPQL-запросы на основе имен методов (Query Derivation).

### 3.7. Обновление REST-контроллера
Класс `PersonController` переписан для использования `PersonRepository` через constructor injection. Добавлены новые endpoint-ы:
- `GET /persons/by-city?city=...`
- `GET /persons/younger-than?age=...`
- `GET /persons/by-name-surname?name=...&surname=...` (возвращает HTTP 200 с JSON или HTTP 404, если не найдено, благодаря `Optional`).

### 3.8. Очистка старого кода
Файлы `schema.sql` и `data.sql` удалены из проекта. Класс `PersonDao` удален.

## 4. Проблемы и их решения

### 4.1. Конфликт инициализации БД (Liquibase vs schema.sql)
**Симптом:** При запуске возникали ошибки дублирования таблиц или конфликты блокировок.
**Причина:** Spring Boot по умолчанию пытался выполнить `schema.sql`/`data.sql` параллельно или до Liquibase.
**Решение:** Явное отключение стандартной инициализации через `spring.sql.init.mode=never` и установка `spring.jpa.hibernate.ddl-auto=validate`, передав полную ответственность за схему Liquibase.

### 4.2. Обработка Optional для составного ключа
**Симптом:** Метод `findByNameAndSurname` мог теоретически вернуть несколько записей, так как `age` является частью первичного ключа, но не участвует в поиске.
**Решение:** Использование возвращаемого типа `Optional<Person>`. Spring Data JPA корректно обрабатывает этот случай, возвращая первую найденную запись, обернутую в `Optional`, что позволяет элегантно обработать сценарий "не найдено" через `ResponseEntity.notFound().build()` в контроллере.

## 5. Чек-лист самопроверки
- Создана отдельная feature-ветка `jpa-repository`
- Добавлен `docker-compose.yml` для запуска PostgreSQL
- Добавлена зависимость `liquibase-core` в `pom.xml`
- Отключён стандартный механизм `schema.sql` через `spring.sql.init.mode=never`
- Создана структура директорий `db/changelog/changes/` с SQL-файлами
- Главный файл `db.changelog-master.yaml` корректно ссылается на миграции
- Миграции используют формат Liquibase с комментариями `--changeset` и `--rollback`
- Создан интерфейс `PersonRepository`, расширяющий `JpaRepository`
- Реализованы методы: по городу, по возрасту (меньше, с сортировкой), по имени и фамилии (`Optional`)
- Контроллер обновлен и предоставляет доступ ко всем новым методам
- При запуске в логах видны сообщения `Liquibase: Update has been successful`
- В базе данных автоматически создаются таблицы `databasechangelog` и `databasechangeloglock`
- Старый код (`PersonDao`, `schema.sql`) удален
- Код закоммичен и отправлен в удалённый репозиторий GitHub в ветку `jpa-repository`

## 6. Полученные результаты

### Работающий REST API после рефакторинга:
- `GET /persons/by-city?city=Moscow` → список из 4 пользователей (HTTP 200)
- `GET /persons/younger-than?age=30` → список пользователей младше 30 лет, отсортированных по возрастанию: Elena (22), Ivan (25), Anna (28), Sergey (29) (HTTP 200)
- `GET /persons/by-name-surname?name=Ivan&surname=Ivanov` → объект пользователя (HTTP 200)
- `GET /persons/by-name-surname?name=Unknown&surname=User` → пустой ответ (HTTP 404)

### Архитектурные улучшения:
- **Декларативность:** Отсутствие boilerplate-кода с `EntityManager` и `createQuery`. Логика запросов читается прямо из сигнатур методов интерфейса.
- **Безопасность типов:** Использование `Optional<Person>` предотвращает `NullPointerException` и явно сигнализирует о возможном отсутствии результата.
- **Версионируемость и идемпотентность:** Liquibase гарантирует, что схема БД всегда соответствует коду, а миграции не применяются повторно.
- **Воспроизводимость среды:** `docker-compose.yml` позволяет любому разработчику поднять идентичное окружение БД одной командой.

### Диагностический опыт:
- Закреплены навыки работы с Query Derivation в Spring Data JPA.
- Освоена интеграция Liquibase в существующий Spring Boot проект с заменой устаревших скриптов инициализации.
- Получен опыт настройки `ddl-auto=validate` для безопасной работы с управляемыми миграциями схемами.

## 7. Вывод
В ходе выполнения задания «8.5. Spring Data JPA» приложение было успешно рефакторировано:
1. Нативный DAO-слой заменен на типобезопасный и лаконичный Spring Data JPA Repository.
2. Реализованы все требуемые методы-запросы, включая сортировку и работу с `Optional`.
3. Внедрен Liquibase для надежного управления версионированием схемы БД с возможностью отката (`rollback`).
4. Добавлен `docker-compose.yml` для стандартизации окружения разработки.

Реализованное решение полностью соответствует требованиям задания, демонстрирует переход от императивного стиля работы с JPA к декларативному и готово к использованию в реальных командных проектах. Код размещен в ветке `jpa-repository` репозитория GitHub.
