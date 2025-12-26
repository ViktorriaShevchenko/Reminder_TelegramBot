package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
public class NotificationTaskRepositoryTest {

    @Autowired
    private NotificationTaskRepository taskRepository;

    @Test
    void testSaveAndFindTask() {
        // Given
        NotificationTask task = new NotificationTask();
        task.setChatId(123456L);
        task.setMessage("Тестовое напоминание");
        task.setNotificationDateTime(LocalDateTime.of(2025, 12, 26, 20, 0));

        // When
        NotificationTask savedTask = taskRepository.save(task);
        List<NotificationTask> foundTasks = taskRepository.findAll();

        // Then
        assertNotNull(savedTask.getId());
        assertEquals(1, foundTasks.size());
        assertEquals("Тестовое напоминание", foundTasks.get(0).getMessage());
    }

    @Test
    void testFindTasksByDateTime() {
        // Given
        LocalDateTime searchTime = LocalDateTime.of(2025, 12, 26, 20, 0);

        NotificationTask task1 = new NotificationTask(123L, "Напоминание 1", searchTime);
        NotificationTask task2 = new NotificationTask(456L, "Напоминание 2",
                searchTime.plusMinutes(1));

        taskRepository.save(task1);
        taskRepository.save(task2);

        // When
        List<NotificationTask> tasks = taskRepository.findTasksByDateTime(searchTime);

        // Then
        assertEquals(1, tasks.size());
        assertEquals("Напоминание 1", tasks.get(0).getMessage());
    }
}
