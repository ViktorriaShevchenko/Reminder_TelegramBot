package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TaskSchedulingService {
    private final Logger logger = LoggerFactory.getLogger(TaskSchedulingService.class);
    private final NotificationTaskRepository taskRepository;
    private final TelegramBot telegramBot;

    @Autowired
    public TaskSchedulingService(NotificationTaskRepository taskRepository, TelegramBot telegramBot) {
        this.taskRepository = taskRepository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledNotifications() {
        LocalDateTime currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        logger.info("Looking for notifications at: {}", currentTime);

        List<NotificationTask> tasks = taskRepository.findTasksByDateTime(currentTime);
        logger.info("Found {} tasks to notify", tasks.size());

        for (NotificationTask task : tasks) {
            sendNotification(task);
        }
    }

    private void sendNotification(NotificationTask task) {
        try {
            String messageText = "⏰ Напоминание: " + task.getMessage();
            SendMessage message = new SendMessage(task.getChatId(), messageText);

            telegramBot.execute(message);

            logger.info("A reminder has been sent to the user {}: {}", task.getChatId(), task.getMessage());

            taskRepository.delete(task);
            logger.info("Task {} deleted after submission", task.getId());
        } catch (Exception e) {
            logger.error("Error sending reminder for task {}: {}", task.getId(), e.getMessage());
        }
    }
}
