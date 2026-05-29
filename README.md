# FitPoint

Мобильное приложение для записи на фитнес-занятия. Платформа объединяет клиентов, тренеров и администраторов в едином пространстве для управления групповыми тренировками.

## Скриншоты

> _Добавьте скриншоты приложения сюда_

## Стек технологий

### Android (клиент)
| Категория | Технология |
|---|---|
| Язык | Kotlin 2.x |
| UI | Jetpack Compose + Material Design 3 |
| DI | Hilt |
| HTTP | Retrofit 2 + OkHttp 4 |
| Асинхронность | Coroutines + StateFlow |
| Локальное хранилище | DataStore Preferences |
| Навигация | Navigation Compose |
| Архитектура | Clean Architecture (data / domain / presentation) |

### Сервер
| Категория | Технология |
|---|---|
| Фреймворк | Ktor |
| БД | PostgreSQL 15 |
| ORM | Exposed |
| DI | Koin |
| Аутентификация | JWT |
| Развёртывание | Docker + Docker Compose |

---

## Архитектура клиента

```
com.example.fitnessapp/
  app/              # Application, MainActivity
  data/
    api/            # ApiService (Retrofit), AuthInterceptor, DTO-классы
    local/          # TokenDataStore, UserDataStore, ThemeDataStore
    repository/     # Реализации репозиториев
  domain/
    model/          # Доменные модели
    repository/     # Интерфейсы репозиториев
  presentation/
    auth/           # Вход, регистрация
    home/           # Лента занятий (клиент)
    mybookings/     # Мои записи (клиент)
    booking/        # Детали занятия + RuTube-плеер
    search/         # Поиск + история
    profile/        # Профиль, тема, цветовой акцент
    coach/          # Интерфейс тренера
    admin/          # Интерфейс администратора
    main/           # Bottom navigation (клиент)
    theme/          # FitnessAppTheme, AccentColor
    navigation/     # Routes
  di/               # NetworkModule, RepositoryModule
```

---

## Роли пользователей

### Клиент (`userTypeId = 3`)
- Просмотр всех занятий и запись на них
- Список своих записей с возможностью отмены
- Детальная карточка занятия со встроенным видеоплеером (RuTube)
- Поиск занятий с историей запросов

### Тренер (`userTypeId = 2`)
- Создание занятий (название, дата/время, кол-во мест, описание)
- Редактирование и удаление своих занятий
- Просмотр списка записавшихся участников

### Администратор (`userTypeId = 1`)
- Все функции тренера плюс доступ к любому занятию
- Управление пользователями: создание клиентов и тренеров, редактирование, удаление
- Справочники: типы тренеров и типы занятий (CRUD)

---

## Запуск сервера

### Требования
- Docker Desktop (или Docker Engine + Compose plugin)

### Команда

```bash
cd server
docker-compose up --build -d
```

Сервер запустится на `http://localhost:8080`. PostgreSQL поднимается автоматически и ждёт готовности перед стартом приложения.

### Переменные окружения (docker-compose.yml)

| Переменная | Значение по умолчанию | Описание |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://db:5432/fitness` | JDBC-строка подключения |
| `DB_USER` | `postgres` | Пользователь БД |
| `DB_PASSWORD` | `1234` | Пароль БД |
| `JWT_SECRET` | `change-me-in-production` | Секрет для подписи токенов |

> ⚠️ Смените `JWT_SECRET` перед деплоем в продакшн.

### Остановка

```bash
docker-compose down          # остановить контейнеры
docker-compose down -v       # остановить и удалить данные БД
```

---

## API

**Base URL:** `http://<host>:8080/`

| Метод | Путь | Авторизация | Описание |
|---|---|---|---|
| POST | `auth/login` | — | Вход: `{email, password}` → `{token}` |
| POST | `auth/register` | — | Регистрация: `{fio, phone, email, password, userTypeId}` |
| GET | `bookings` | Bearer | Все занятия |
| GET | `bookings/my` | Bearer | Занятия клиента |
| GET | `bookings/coach` | Bearer (COACH) | Занятия тренера |
| GET | `bookings/search?q=` | Bearer | Поиск по названию |
| POST | `bookings/{id}/join` | Bearer | Записаться на занятие |
| POST | `bookings` | Bearer (COACH) | Создать занятие |
| PATCH | `bookings/{id}` | Bearer (COACH/ADMIN) | Изменить занятие |
| DELETE | `bookings/{id}` | Bearer (COACH/ADMIN) | Удалить занятие |
| GET | `bookings/{id}/participants` | Bearer (COACH/ADMIN) | Список участников |
| GET | `admin/users` | Bearer (ADMIN) | Все пользователи |
| POST | `admin/users` | Bearer (ADMIN) | Создать пользователя |
| PATCH | `admin/users/{id}` | Bearer (ADMIN) | Изменить пользователя |
| DELETE | `admin/users/{id}` | Bearer (ADMIN) | Удалить пользователя |
| GET | `admin/coach-types` | Bearer (ADMIN) | Типы тренеров |
| POST | `admin/coach-types` | Bearer (ADMIN) | Создать тип тренера |
| PATCH | `admin/coach-types/{id}` | Bearer (ADMIN) | Изменить тип тренера |
| DELETE | `admin/coach-types/{id}` | Bearer (ADMIN) | Удалить тип тренера |
| GET | `admin/workouts` | Bearer (ADMIN) | Типы занятий |
| POST | `admin/workouts` | Bearer (ADMIN) | Создать тип занятия |
| PATCH | `admin/workouts/{id}` | Bearer (ADMIN) | Изменить тип занятия |
| DELETE | `admin/workouts/{id}` | Bearer (ADMIN) | Удалить тип занятия |

---

## Сборка Android-приложения

### Требования
- Android Studio Hedgehog или новее
- JDK 17+
- Android SDK 36

### Настройка BASE_URL

В файле `app/build.gradle.kts` укажите IP-адрес вашего сервера:

```kotlin
debug {
    buildConfigField("String", "BASE_URL", "\"http://192.168.1.197:8080/\"")
}
```

### Gradle-команды

```bash
./gradlew assembleDebug      # сборка debug APK
./gradlew assembleRelease    # сборка release APK
./gradlew installDebug       # установка на подключённое устройство
./gradlew test               # юнит-тесты
./gradlew lint               # lint-проверка
./gradlew clean              # очистка сборки
```

---

## Требования к устройству

- Android 7.0 (API 24) и выше
- Подключение к той же сети, что и сервер (или доступ к серверу по IP)

---

## Особенности реализации

- **Тема без мерцания** — начальное значение темы читается синхронно через `runBlocking` до первого кадра
- **Цветовой акцент** — 6 вариантов палитры (фиолетовый, синий, бирюзовый, зелёный, оранжевый, красный), сохраняется в DataStore
- **RuTube-плеер** — ссылки на `rutube.ru/video/{id}` автоматически распознаются в описании занятия и открываются во встроенном WebView
- **Pull-to-refresh** — работает в том числе на пустых экранах (контент обёрнут в `LazyColumn`)
- **Роль при входе** — `userTypeId` сохраняется в DataStore, NavHost направляет к нужному интерфейсу без лишних запросов
- **История поиска** — хранится в DataStore, максимум 10 записей, дедупликация с сохранением порядка

---

## Лицензия

MIT
