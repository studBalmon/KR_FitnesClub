# FitPoint

Мобильное приложение для записи на фитнес-занятия. Платформа объединяет клиентов, тренеров и администраторов в едином пространстве для управления групповыми тренировками.

## Скриншоты

<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/673f72a4-6b77-4b8f-961a-9732f2df2043" />
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/91e17456-a226-4149-90ea-054bf21e83cc" />
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/e372414e-f99a-49ee-8124-5c6e8a79ec03" />
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/eba2768f-0e90-4d63-b919-b78f683fb50b" />
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/d355e8d0-ba0c-4fa6-a6e7-db4f72850858" />
<img width="300" height="600" alt="image" src="https://github.com/user-attachments/assets/fa97a211-d03b-4ff1-8dda-2d0e70c5f5be" />


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
