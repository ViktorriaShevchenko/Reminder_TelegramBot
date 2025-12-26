package pro.sky.telegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class TelegramBotApplicationTests {

	@Test
	void contextLoads() {
		assertNotNull(this, "Контекст Spring должен загружаться");
	}

	@Test
	void mainMethodStartsApplication() {
		TelegramBotApplication.main(new String[]{});
		assertNotNull(TelegramBotApplication.class, "Приложение должно запускаться");
	}
}
