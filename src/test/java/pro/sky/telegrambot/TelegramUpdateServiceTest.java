package pro.sky.telegrambot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.TelegramUpdateService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramUpdateServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationTaskRepository taskRepository;

    @InjectMocks
    private TelegramUpdateService telegramUpdateService;

    private final Pattern reminderPattern = Pattern.compile(
            "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)"
    );

    @Test
    void testPatternMatchesCorrectFormat() {
        // Given
        String correctMessage = "26.12.2025 20:00 Тестовое напоминание";

        // When
        boolean matches = reminderPattern.matcher(correctMessage).matches();

        // Then
        assertTrue(matches, "Паттерн должен совпадать с корректным форматом");
    }

    @Test
    void testPatternDoesNotMatchIncorrectFormat() {
        // Given
        String incorrectMessage = "неправильный формат";

        // When
        boolean matches = reminderPattern.matcher(incorrectMessage).matches();

        // Then
        assertFalse(matches, "Паттерн не должен совпадать с некорректным форматом");
    }

    @Test
    void testDateTimeParsing() {
        // Given
        String dateTimeString = "26.12.2025 20:00";

        // When
        LocalDateTime parsedDateTime = LocalDateTime.parse(
                dateTimeString,
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        );

        // Then
        assertNotNull(parsedDateTime);
        assertEquals(2025, parsedDateTime.getYear());
        assertEquals(12, parsedDateTime.getMonthValue());
        assertEquals(26, parsedDateTime.getDayOfMonth());
        assertEquals(20, parsedDateTime.getHour());
        assertEquals(0, parsedDateTime.getMinute());
    }

    @Test
    void testProcessStartCommand() {
        // Given
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/start");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123456L);

        List<Update> updates = Collections.singletonList(update);

        // When
        int result = telegramUpdateService.process(updates);

        // Then
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
        verify(telegramBot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void testProcessValidReminder() {
        // Given
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("26.12.2025 20:00 Тестовое напоминание");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123456L);

        List<Update> updates = Collections.singletonList(update);

        // When
        int result = telegramUpdateService.process(updates);

        // Then
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
        verify(taskRepository, times(1)).save(any(NotificationTask.class));
        verify(telegramBot, atLeastOnce()).execute(any(SendMessage.class));
    }

    @Test
    void testProcessInvalidReminderFormat() {
        // Given
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("неправильный формат");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123456L);

        List<Update> updates = Collections.singletonList(update);

        // When
        int result = telegramUpdateService.process(updates);

        // Then
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
        verify(taskRepository, never()).save(any(NotificationTask.class));
        verify(telegramBot, times(1)).execute(any(SendMessage.class));
    }
}
