package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramUpdateService implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramUpdateService.class);
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;
    private final Pattern reminderPattern = Pattern.compile(
            "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)"
    );

    @Autowired
    public TelegramUpdateService(TelegramBot telegramBot,
                                 NotificationTaskRepository taskRepository) {
        this.telegramBot = telegramBot;
        this.notificationTaskRepository = taskRepository;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
        logger.info("Telegram bot listener initialized");
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            if (update.message() != null && update.message().text() != null) {
                String messageText = update.message().text();
                Long chatId = update.message().chat().id();

                logger.info("Received message: '{}' from chatId: {}", messageText, chatId);

                if ("/start".equals(messageText)) {
                    sendWelcomeMessage(chatId);
                } else {
                    processReminder(chatId, messageText);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = "Привет! Я бот-напоминалка!\n\n" +
                "Отправь мне сообщение в формате:\n" +
                "01.01.2022 20:00 Сделать домашнюю работу\n\n" +
                "Я напомню тебе в указанное время!";

        sendMessage(chatId, welcomeText);
        logger.info("Sent welcome message to chatId: {}", chatId);
    }

    private void processReminder(Long chatId, String messageText) {
        Matcher matcher = reminderPattern.matcher(messageText);

        if (matcher.matches()) {
            logger.info("Pattern matched! Group1: '{}', Group3: '{}'",
                    matcher.group(1), matcher.group(3));

            try {
                String dateTimeString = matcher.group(1);
                String reminderText = matcher.group(3);

                // Парсим дату и время
                LocalDateTime dateTime = LocalDateTime.parse(
                        dateTimeString,
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                );

                // Проверяем, что время в будущем
                if (dateTime.isBefore(LocalDateTime.now())) {
                    sendMessage(chatId, "Время напоминания должно быть в будущем!");
                    return;
                }

                // Создаем и сохраняем задачу
                NotificationTask task = new NotificationTask(chatId, reminderText, dateTime);
                notificationTaskRepository.save(task);

                sendMessage(chatId, "Напоминание сохранено! Я напомню в указанное время.");
                logger.info("Saved reminder for chatId: {}, time: {}, text: {}",
                        chatId, dateTime, reminderText);

            } catch (Exception e) {
                logger.error("Error parsing date from: '{}'", messageText, e);
                sendMessage(chatId, "Неверный формат даты. Используйте: дд.мм.гггг чч:мм");
            }
        } else {
            logger.info("Pattern did NOT match for: '{}'", messageText);
            sendMessage(chatId, "Неверный формат. Используйте: 01.01.2022 20:00 Текст напоминания");
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage(chatId, text);
            telegramBot.execute(message);
            logger.info("Sent message to chatId {}: {}", chatId, text);
        } catch (Exception e) {
            logger.error("Error sending message to chatId {}: {}", chatId, e.getMessage());

        }
    }
}
