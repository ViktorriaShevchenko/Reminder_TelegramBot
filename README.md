# Telegram Reminder Bot

Бот для создания напоминаний в Telegram.

## Функциональность
- Обработка команды `/start`
- Сохранение напоминаний в формате: `01.01.2022 20:00 Текст напоминания`
- Автоматическая отправка напоминаний в указанное время

## Технологии
- Java 11
- Spring Boot 2.6.5
- PostgreSQL
- Liquibase
- Telegram Bot API

## Запуск
1. Установите PostgreSQL
2. Создайте базу данных `reminder_bot_db`
3. Настройте `application.properties`
4. Запустите `TelegramBotApplication`
