# Руководство по настройке аватара бота

## Как добавить фото аватара бота

### Шаг 1: Подготовьте изображение
- **Рекомендуемый размер**: 120x120 пикселей (минимум) или 200x200 пикселей (оптимально)
- **Формат**: PNG или JPG
- **Форма**: Квадратная (будет автоматически обрезана в круг)
- **Качество**: Четкое, не размытое изображение

### Шаг 2: Добавьте файл в проект
1. Откройте папку `app/src/main/res/drawable/`
2. Скопируйте туда ваше изображение
3. Переименуйте его в `bot_avatar.png` или `bot_avatar.jpg`
   - Имя файла должно быть строчными буквами
   - Только буквы, цифры и подчеркивание
   - Пример: `bot_avatar.png`, `bot_avatar_new.png`

### Шаг 3: Активируйте изображение в коде
Откройте файл `app/src/main/java/com/bahilai/gigadanya/ui/components/ChatHeader.kt`

Найдите секцию с комментарием "Вариант 1" (строки ~42-53) и **раскомментируйте** код:

```kotlin
// Было (закомментировано):
/*
Image(
    painter = painterResource(id = R.drawable.bot_avatar),
    ...
)
*/

// Должно стать (раскомментировано):
Image(
    painter = painterResource(id = R.drawable.bot_avatar),
    contentDescription = "Bot Avatar",
    modifier = Modifier
        .size(40.dp)
        .clip(CircleShape),
    contentScale = ContentScale.Crop
)
```

### Шаг 4: Закомментируйте старую иконку
Найдите секцию с "Вариант 2" (строки ~55-62) и **закомментируйте** Icon:

```kotlin
// Было (работает):
Icon(
    imageVector = Icons.Default.AccountCircle,
    ...
)

// Должно стать (закомментировано):
/*
Icon(
    imageVector = Icons.Default.AccountCircle,
    contentDescription = "Bot Avatar",
    modifier = Modifier.size(40.dp),
    tint = MaterialTheme.colorScheme.onPrimaryContainer
)
*/
```

### Шаг 5: Пересоберите проект
```bash
# В терминале Android Studio:
./gradlew clean
./gradlew build
```

Или через меню: **Build** → **Clean Project** → **Build** → **Rebuild Project**

---

## Альтернативный вариант: Использовать URL изображения

Если изображение находится в интернете, можно загрузить его по URL.

### Добавьте библиотеку Coil (если еще не добавлена)

В файл `app/build.gradle.kts` в секцию `dependencies` добавьте:
```kotlin
implementation("io.coil-kt:coil-compose:2.5.0")
```

### Обновите ChatHeader.kt:

```kotlin
import coil.compose.AsyncImage

// Вместо Image используйте:
AsyncImage(
    model = "https://example.com/bot_avatar.png",
    contentDescription = "Bot Avatar",
    modifier = Modifier
        .size(40.dp)
        .clip(CircleShape),
    contentScale = ContentScale.Crop,
    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
    error = painterResource(id = R.drawable.ic_launcher_foreground)
)
```

---

## Проверка

После выполнения всех шагов:
1. Запустите приложение на устройстве/эмуляторе
2. Проверьте, что аватар бота отображается в верхней части экрана
3. Аватар должен быть круглым размером 40dp

---

## Устранение проблем

### "R.drawable.bot_avatar не найден"
- Проверьте имя файла (только строчные буквы, без пробелов)
- Убедитесь, что файл находится в `app/src/main/res/drawable/`
- Выполните **Build** → **Clean Project** и пересоберите

### Изображение не отображается
- Проверьте размер файла (не более 1 МБ рекомендуется)
- Проверьте формат (PNG или JPG)
- Убедитесь, что раскомментировали код Image и закомментировали Icon

### Изображение растянуто или обрезано неправильно
- Используйте квадратное изображение
- Попробуйте другой `ContentScale`:
  - `ContentScale.Crop` - обрезает края (по умолчанию)
  - `ContentScale.Fit` - вписывает полностью
  - `ContentScale.FillBounds` - растягивает на весь размер


