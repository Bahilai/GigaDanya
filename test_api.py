#!/usr/bin/env python3
"""
Скрипт для тестирования Yandex AI Studio Agent API
Используйте этот скрипт для проверки, правильно ли работает ваш API
"""

import requests
import json
import sys

# Замените эти значения на ваши реальные credentials из local.properties
API_KEY = "AQVN20OMevZHfQyr7bND7Vmv8EFyGpcGjyEYws63"
FOLDER_ID = "b1g9c4e7uvjon9qd842q"
AGENT_ID = "fvtbqlta7fhgog296d1g"

# URL для API
API_URL = "https://rest-assistant.api.cloud.yandex.net/v1/responses"

def test_api():
    """Тестирование API"""
    
    headers = {
        "Authorization": f"Api-Key {API_KEY}",
        "x-folder-id": FOLDER_ID,
        "Content-Type": "application/json"
    }
    
    payload = {
        "prompt": {
            "id": AGENT_ID
        },
        "input": "Привет! Как дела?",
        "stream": False
    }
    
    print("🚀 Отправка запроса к Yandex AI Studio Agent API...")
    print(f"📍 URL: {API_URL}")
    print(f"📋 Headers: {json.dumps({k: v[:20] + '...' if k == 'Authorization' else v for k, v in headers.items()}, indent=2)}")
    print(f"📦 Payload: {json.dumps(payload, indent=2, ensure_ascii=False)}")
    print("\n" + "="*60 + "\n")
    
    try:
        response = requests.post(API_URL, headers=headers, json=payload, timeout=30)
        
        print(f"✅ Статус код: {response.status_code}")
        print(f"📨 Заголовки ответа:")
        for key, value in response.headers.items():
            print(f"  {key}: {value}")
        print("\n" + "="*60 + "\n")
        
        if response.status_code == 200:
            print("✅ УСПЕХ! API вернул ответ:")
            print(json.dumps(response.json(), indent=2, ensure_ascii=False))
            
            # Попробуем извлечь текст ответа
            data = response.json()
            
            # Формат 1: AI Studio Agent
            if "output" in data:
                text = data.get("output", [{}])[0].get("content", [{}])[0].get("text")
                if text:
                    print(f"\n💬 Извлеченный текст (формат AI Studio): {text}")
                    return True
            
            # Формат 2: Foundation Models
            if "result" in data:
                text = data.get("result", {}).get("alternatives", [{}])[0].get("message", {}).get("text")
                if text:
                    print(f"\n💬 Извлеченный текст (формат Foundation Models): {text}")
                    return True
            
            print("\n⚠️ Не удалось извлечь текст из ответа. Проверьте структуру.")
            return False
            
        else:
            print(f"❌ ОШИБКА! Код: {response.status_code}")
            print(f"📄 Тело ответа:")
            try:
                print(json.dumps(response.json(), indent=2, ensure_ascii=False))
            except:
                print(response.text)
            return False
            
    except requests.exceptions.Timeout:
        print("❌ ОШИБКА: Превышено время ожидания (timeout)")
        return False
    except requests.exceptions.ConnectionError:
        print("❌ ОШИБКА: Не удалось подключиться к серверу")
        return False
    except Exception as e:
        print(f"❌ ОШИБКА: {str(e)}")
        return False

if __name__ == "__main__":
    print("\n" + "="*60)
    print("  Тестирование Yandex AI Studio Agent API")
    print("="*60 + "\n")
    
    success = test_api()
    
    print("\n" + "="*60)
    if success:
        print("✅ Тест пройден! API работает корректно.")
        print("   Если приложение не работает, проверьте:")
        print("   1. Правильность credentials в local.properties")
        print("   2. Логи в Logcat (теги: ChatViewModel, RetrofitInstance)")
        print("   3. Интернет-соединение на устройстве/эмуляторе")
    else:
        print("❌ Тест не пройден. Проверьте credentials и доступ к API.")
    print("="*60 + "\n")
    
    sys.exit(0 if success else 1)

