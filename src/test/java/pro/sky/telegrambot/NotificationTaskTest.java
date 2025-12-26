package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import pro.sky.telegrambot.model.NotificationTask;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class NotificationTaskTest {

    @Test
    void testNotificationTaskCreation() {
        // Given
        Long chatId = 123456L;
        String message = "Тестовое напоминание";
        LocalDateTime dateTime = LocalDateTime.of(2025, 12, 26, 20, 0);

        // When
        NotificationTask task = new NotificationTask(chatId, message, dateTime);

        // Then
        assertNotNull(task);
        assertEquals(chatId, task.getChatId());
        assertEquals(message, task.getMessage());
        assertEquals(dateTime, task.getNotificationDateTime());
    }

    @Test
    void testDefaultConstructor() {
        // When
        NotificationTask task = new NotificationTask();

        // Then
        assertNotNull(task);
        assertNull(task.getId());
        assertNull(task.getChatId());
        assertNull(task.getMessage());
        assertNull(task.getNotificationDateTime());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        NotificationTask task = new NotificationTask();
        Long id = 1L;
        Long chatId = 123456L;
        String message = "Новое сообщение";
        LocalDateTime dateTime = LocalDateTime.now();

        // When
        task.setId(id);
        task.setChatId(chatId);
        task.setMessage(message);
        task.setNotificationDateTime(dateTime);

        // Then
        assertEquals(id, task.getId());
        assertEquals(chatId, task.getChatId());
        assertEquals(message, task.getMessage());
        assertEquals(dateTime, task.getNotificationDateTime());
    }
}
