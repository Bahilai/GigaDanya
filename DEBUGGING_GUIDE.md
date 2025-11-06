# Руководство по отладке GigaDanya

## Сделанные улучшения

### 1. Улучшенная обработка JSON
- Добавлена более гибкая конфигурация Gson с `.setLenient()` и `.serializeNulls()`
- Добавлена поддержка разных форматов ответов от Yandex API
- Модели данных теперь имеют значения по умолчанию для nullable полей

### 2. Подробное логирование
Теперь приложение логирует:
- URL запросов и заголовки
- Коды ответов и заголовки ответов
- Полное тело ответа (в режиме DEBUG)
- Детали ошибок с HTTP кодами и телом ошибки

### 3. Улучшенная обработка ошибок
- Добавлена обработка `HttpException` с логированием кода и тела ошибки
- Более детальные сообщения об ошибках для пользователя
- Логирование всех исключений с полным stack trace

## Как отладить приложение

### Шаг 1: Проверьте логи в Logcat

В Android Studio откройте Logcat и отфильтруйте по тегам:
- `ChatViewModel` - логи ViewModel
- `RetrofitInstance` - логи сетевых запросов
- `HttpLoggingInterceptor` - полные HTTP запросы и ответы

### Шаг 2: Проверьте API credentials

Убедитесь, что в файле `local.properties` правильно заданы:
```
YANDEX_API_KEY=ваш_ключ
YANDEX_FOLDER_ID=ваш_folder_id
YANDEX_AGENT_ID=ваш_agent_id
```

### Шаг 3: Проверьте формат ответа API

В Logcat найдите строку "Response received:" и проверьте структуру JSON.

Yandex AI Studio Agent API может возвращать ответы в двух форматах:

**Формат 1 (AI Studio Agent):**
```json
{
  "output": [
    {
      "content": [
        {
          "text": "Текст ответа",
          "type": "text"
        }
      ]
    }
  ],
  "status": "success"
}
```

**Формат 2 (Foundation Models):**
```json
{
  "result": {
    "alternatives": [
      {
        "message": {
          "role": "assistant",
          "text": "Текст ответа"
        },
        "status": "ALTERNATIVE_STATUS_FINAL"
      }
    ]
  }
}
```

Приложение теперь поддерживает оба формата.

### Шаг 4: Проверьте интернет-соединение

1. Проверьте, что устройство/эмулятор имеет доступ к интернету
2. Проверьте, что Yandex API доступен (можно проверить через браузер)

### Шаг 5: Тестирование API через curl

Используйте следующую команду для тестирования API:

```bash
curl -X POST https://rest-assistant.api.cloud.yandex.net/v1/responses \
  -H "Authorization: Api-Key ВАШ_API_KEY" \
  -H "x-folder-id: ВАШ_FOLDER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": {
      "id": "ВАШ_AGENT_ID"
    },
    "input": "Привет!",
    "stream": false
  }'
```

## Типичные ошибки и их решения

### Ошибка: "HTTP Error 401"
**Причина:** Неправильный API ключ
**Решение:** Проверьте `YANDEX_API_KEY` в `local.properties`

### Ошибка: "HTTP Error 403"
**Причина:** Нет доступа к ресурсу
**Решение:** 
- Проверьте `YANDEX_FOLDER_ID`
- Убедитесь, что у API ключа есть права на folder
- Проверьте, что Agent ID правильный

### Ошибка: "HTTP Error 404"
**Причина:** Неправильный endpoint или Agent ID
**Решение:** 
- Проверьте `YANDEX_AGENT_ID`
- Убедитесь, что используется правильный URL: `https://rest-assistant.api.cloud.yandex.net/v1/`

### Ошибка: "Не удалось получить ответ от агента. Ответ пуст."
**Причина:** API вернул ответ, но в неожиданном формате
**Решение:** 
- Проверьте логи и найдите "Response received:"
- Сравните структуру с ожидаемыми форматами выше
- Если формат отличается, обновите модели данных

### Ошибка: "Превышено время ожидания"
**Причина:** Медленное интернет-соединение или проблемы с API
**Решение:**
- Проверьте интернет-соединение
- Увеличьте timeout в `RetrofitInstance.kt` (сейчас 60 секунд)

## Дополнительная информация

### Используемые библиотеки
- Retrofit 2 - для HTTP запросов
- Gson - для сериализации/десериализации JSON
- OkHttp Logging Interceptor - для логирования запросов
- Coil - для загрузки изображений

### Полезные ссылки
- [Yandex Cloud AI Documentation](https://cloud.yandex.ru/docs/ai/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Gson Documentation](https://github.com/google/gson)

## Контрольный список перед запуском

- [ ] API credentials заданы в `local.properties`
- [ ] Приложение пересобрано после изменения `local.properties`
- [ ] Интернет-соединение работает
- [ ] В `AndroidManifest.xml` есть разрешение `INTERNET`
- [ ] В Logcat включены логи (не отфильтрованы)
- [ ] Проверен доступ к Yandex API через curl или браузер

