# Руководство по изменению иконки приложения

## Быстрый способ (рекомендуется)

### Через Android Studio:
1. Откройте Android Studio
2. В дереве проекта найдите папку `app/src/main/res`
3. Правый клик на `res` → **New** → **Image Asset**
4. В окне "Configure Image Asset":
   - **Icon Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: `ic_launcher`
   - **Foreground Layer**: Path → выберите вашу иконку
   - **Background Layer**: выберите цвет или изображение фона
5. Нажмите **Next** → **Finish**

Android Studio автоматически создаст все необходимые размеры!

---

## Требования к исходному изображению

- **Рекомендуемый размер**: 512x512 px или 1024x1024 px
- **Формат**: PNG с прозрачностью (для foreground)
- **Отступы**: оставьте ~20% отступы по краям (иконка будет обрезаться на некоторых устройствах)

---

## Размеры для разных экранов

Если вы хотите создать иконки вручную, вам нужны следующие размеры:

### Обычная иконка (ic_launcher.png):
- `mipmap-mdpi/ic_launcher.png` - 48x48 px
- `mipmap-hdpi/ic_launcher.png` - 72x72 px
- `mipmap-xhdpi/ic_launcher.png` - 96x96 px
- `mipmap-xxhdpi/ic_launcher.png` - 144x144 px
- `mipmap-xxxhdpi/ic_launcher.png` - 192x192 px

### Круглая иконка (ic_launcher_round.png):
Те же размеры, но изображение должно быть круглым

### Adaptive Icon (Android 8.0+):
- `mipmap-mdpi/ic_launcher_foreground.png` - 108x108 px
- `mipmap-hdpi/ic_launcher_foreground.png` - 162x162 px
- `mipmap-xhdpi/ic_launcher_foreground.png` - 216x216 px
- `mipmap-xxhdpi/ic_launcher_foreground.png` - 324x324 px
- `mipmap-xxxhdpi/ic_launcher_foreground.png` - 432x432 px

---

## Ручное создание иконок

### Шаг 1: Подготовьте изображения
Создайте изображения всех размеров, указанных выше.

### Шаг 2: Замените файлы
Поместите файлы в соответствующие папки:
```
app/src/main/res/
  ├── mipmap-mdpi/
  │   ├── ic_launcher.png
  │   └── ic_launcher_round.png
  ├── mipmap-hdpi/
  │   ├── ic_launcher.png
  │   └── ic_launcher_round.png
  ├── mipmap-xhdpi/
  │   ├── ic_launcher.png
  │   └── ic_launcher_round.png
  ├── mipmap-xxhdpi/
  │   ├── ic_launcher.png
  │   └── ic_launcher_round.png
  └── mipmap-xxxhdpi/
      ├── ic_launcher.png
      └── ic_launcher_round.png
```

### Шаг 3: Обновите Adaptive Icon (опционально)
Отредактируйте `app/src/main/res/mipmap-anydpi/ic_launcher.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
```

### Шаг 4: Пересоберите проект
```bash
./gradlew clean
./gradlew build
```

---

## Онлайн-инструменты для создания иконок

Если у вас есть только одно изображение, используйте эти сервисы для генерации всех размеров:

1. **Android Asset Studio** (Google):
   - https://romannurik.github.io/AndroidAssetStudio/
   - Загрузите изображение → скачайте ZIP со всеми размерами

2. **AppIcon.co**:
   - https://appicon.co/
   - Поддерживает Android и iOS

3. **MakeAppIcon**:
   - https://makeappicon.com/
   - Генерирует все размеры автоматически

---

## Проверка иконки

После установки новой иконки:
1. Удалите старую версию приложения с устройства/эмулятора
2. Пересоберите проект: Build → Clean Project → Build → Rebuild Project
3. Установите приложение заново
4. Проверьте иконку на главном экране

---

## Советы по дизайну

- **Простота**: Иконка должна быть простой и узнаваемой даже в маленьком размере
- **Контраст**: Используйте контрастные цвета
- **Центрирование**: Основной элемент должен быть в центре
- **Отступы**: Оставьте 15-20% отступы по краям
- **Форма**: Помните, что на разных устройствах иконка может быть круглой, квадратной или скругленной

