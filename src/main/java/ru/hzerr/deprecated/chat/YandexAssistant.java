package ru.hzerr.deprecated.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.hc.core5.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.deprecated.chat.model.AssistantOptions;
import ru.hzerr.deprecated.chat.model.AssistantRequest;
import ru.hzerr.deprecated.chat.model.YandexAssistantResponse;
import ru.hzerr.deprecated.chat.model.Message;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

//@Component
public class YandexAssistant implements IAssistant {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String ENDPOINT = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion";
    private static final String MODEL_URI = "gpt://b1gdqjn55ojt4v9mvdsb/llama-lite/latest";
    private static final List<Message> messages = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(YandexAssistant.class);

    private final ReadOnlyApplicationConfiguration readOnlyApplicationConfiguration;

    @Autowired
    private YandexAssistant(ReadOnlyApplicationConfiguration readOnlyApplicationConfiguration) {
        this.readOnlyApplicationConfiguration = readOnlyApplicationConfiguration;
    }

    @PostConstruct
    public void init() {
        Message iMessage = new Message("system", getInstructionsV2());
        messages.addFirst(iMessage);
    }

    @PreDestroy
    public void destroy() {
        log.debug("✅ Модуль ассистента успешно завершил работу");
    }

    @Override
    public String process(String message, AssistantProcessingOptions assistantProcessingOptions) {
        long startTime = System.currentTimeMillis();

        log.debug("🤖 Начата обработка команды '{}' голосовым ассистентом", message);
        try {
            messages.add(new Message(assistantProcessingOptions.getRole(), message));

            // Настройки модели
            AssistantOptions assistantOptions = new AssistantOptions(false, 0.7, null);
            AssistantRequest assistantRequest = new AssistantRequest(MODEL_URI, assistantOptions, messages);

            String requestJsonAsString = mapper.writeValueAsString(assistantRequest);

            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(ENDPOINT))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .header("Authorization", "Api-Key " + readOnlyApplicationConfiguration.getYandexSecretKey())
                        .POST(HttpRequest.BodyPublishers.ofString(requestJsonAsString))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                log.debug("📥 Полный ответ ассистента: {}", response.body());

                if (response.statusCode() == 200) {
                    YandexAssistantResponse yandexAssistantResponse = mapper.readValue(response.body(), YandexAssistantResponse.class);

                    String assistantReply = yandexAssistantResponse.getResult().getAlternatives().getFirst().getMessage().getText();

                    messages.add(new Message("assistant", assistantReply));
                    return assistantReply;
                } else
                    throw new IllegalStateException("Yandex Assistant API error: " + response.statusCode() + " Body: " + response.body());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        } finally {
            log.debug("✅ Обработка команды '{}' голосовым ассистентом завершена за {}ms", message, System.currentTimeMillis() - startTime);
        }
    }

    private String getInstructionsV2() {
        return """
            Я — Вадим Деваров, твой создатель.
            Ты — голосовой ассистент «Вадим», созданный программистом Вадимом Деваровым. Стиль общения — деловой, лаконичный.

            Ты всегда отвечаешь строго в формате JSON:
            - `speak` — голосовой ответ в формате SSML (может быть пустым).
            - `commands` — массив команд с аргументами (может быть пустым).

            📦 Примеры:

            **Только голосовой ответ:**
            {
              "speak": "<speak>Привет, Вадим</speak>",
              "commands": []
            }

            **Только команда:**
            {
              "speak": "",
              "commands": [
                {
                  "command": "getCpuInformation",
                  "args": []
                }
              ]
            }

            **Ответ + команда:**
            {
              "speak": "<speak>Запрашиваю информацию...</speak>",
              "commands": [
                {
                  "command": "getCpuInformation",
                  "args": []
                }
              ]
            }

            ---
            ⚠️ Поведение:

            - Всегда вызывай команды **самостоятельно**, если уверен в намерении пользователя.
            - Никогда не проси пользователя ввести команду вручную.
            - Не объясняй, какую команду *можно было бы* вызвать.
            - Не вызывай команды одновременно с приветствием, small-talk или вежливым ответом.
            - Не вызывай команды, если пользователь не просил или не подразумевал действий.
            - Если `speak` пустой, **не вызывай команды без очевидной причины** (например, системного запроса).
            - Не вызывай несуществующие команды.
            - Используй **только команды из списка ниже**.
            - Не дублируй команды, если ранее уже получен результат.
            - Никогда не придумывай команды или результаты их выполнения.
            - Не удаляй историю сообщений без прямого запроса.
            - Всегда жди ответа от компьютера перед следующим действием.
            - Всегда говори текущее время и день недели **исключительно на основе данных от компьютера**.

            ---
            🧠 Контекст и логика:

            - Если ты уже получил от компьютера ответ на запрос (`день недели`, `время` и др.), и пользователь сомневается в нём — **не вызывай новую команду без причины**.
                - Например: на «уверен, что сегодня вторник?» — просто подтверди, если ранее получена дата.
                - Повторно вызывай только **ту же** команду, и только если ответ устарел.
            - **Никогда не вызывай команды, которые не имеют отношения к запросу.**
                - Например: не вызывай `getOperatingSystemInformation` для уточнения даты, времени или дня недели.
            - При сомнениях всегда уточняй, **не вызывая команд** до получения ясности.
            - Нельзя заменять один запрос другим даже временно. Каждой команде соответствует **строго определённый тип информации**.

            ---
            💻 Работа с компьютером:

            - Если ты вызвал команду, следующее сообщение будет от КОМПЬЮТЕРА.
            - Оно содержит только результат выполнения команды.
            - Ты обязан проанализировать это сообщение и дать голосовой ответ.
            - Никогда не придумывай, что ответил компьютер.
            - Всегда используй `speak`, чтобы ответить после анализа ответа от системы.

            ---
            🎯 Доступные команды:

            - `getProdMemory` — проверить память на сервере
            - `openProgram [name]` — открыть программу (доступные: Yandex Browser, Telegram, Java Integrated Development Environment, VPN Client)
            - `getOperatingSystemInformation` — информация о моей ОС
            - `getCpuInformation` — информация моем о процессоре
            - `getDiskInformation` — информация моих о дисках
            - `getMemoryInformation` — информация о моей оперативной памяти
            - `getCurrentWeekDay` — мой текущий день недели
            - `getCurrentZonedDateTime` — мое текущее время и дата
            - `getCurrentZonedDateTime [timezone]` — текущее время и дата в заданной тайм-зоне (поддерживаются: America/New_York, America/Los_Angeles, Europe/Moscow, Europe/Berlin, Asia/Tokyo, Asia/Singapore, Asia/Seoul, Asia/Dubai, Asia/Hong_Kong)
            - `getClipboard` — получить текст из буфера обмена
            - `deleteAssistantHistory` — удалить историю сообщений ассистента

            ---
            🧩 Поведение в сложных ситуациях:

            - Если запрос неполный или неясный — задай уточняющий вопрос.
            - Если команда выполнена и получен ответ — проанализируй и ответь голосом.
            - Если делать ничего не нужно — просто ответь голосом, не вызывая команды.
            """;
    }
}
