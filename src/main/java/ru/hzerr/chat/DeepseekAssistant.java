package ru.hzerr.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.*;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class DeepseekAssistant implements IAssistant {

    private static final Logger log = LoggerFactory.getLogger(DeepseekAssistant.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ReadOnlyApplicationConfiguration applicationConfiguration;
    private OpenAIClient openAiClient;

    private final List<ChatCompletionMessageParam> messages = new ArrayList<>();

    @Autowired
    public DeepseekAssistant(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @PostConstruct
    public void init() {
        this.openAiClient = OpenAIOkHttpClient.builder()
                .apiKey(applicationConfiguration.getOpenAIAssistantApiKey())
                .baseUrl(applicationConfiguration.getOpenAIAssistantBaseUrl())
                .timeout(Duration.ofSeconds(30))
                .build();

        // Вставляем системное сообщение (инструкция)
        ChatCompletionSystemMessageParam sys = ChatCompletionSystemMessageParam.builder()
                .content(getInstructionsV2())
                .build();

        messages.add(ChatCompletionMessageParam.ofSystem(sys));

        log.debug("✅ Модуль ассистента инициализирован с baseUrl = {}", applicationConfiguration.getOpenAIAssistantBaseUrl());
    }

    @PreDestroy
    public void destroy() {
        openAiClient.close();
        log.debug("✅ Модуль ассистента успешно завершил работу");
    }

    @Override
    public String process(String userMessage, AssistantProcessingOptions options) {
        long startTime = System.currentTimeMillis();
        log.debug("🤖 Обработка пользовательского сообщения: '{}'", userMessage);

        try {
            // Добавляем сообщение от пользователя
            ChatCompletionUserMessageParam userMessageParam = ChatCompletionUserMessageParam.builder()
                    .content(userMessage)
                    .build();

            ChatCompletionMessageParam userMsg = ChatCompletionMessageParam.ofUser(userMessageParam);
            messages.add(userMsg);

            // Собираем параметры запроса
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model("deepseek-chat")  // или другой идентификатор модели DeepSeek
                    .messages(messages)
                    .temperature(0.7)
                    // при необходимости можно добавить maxTokens, topP и др.
                    .build();

            ChatCompletion response = openAiClient.chat().completions().create(params);
            // лог всего ответа (для отладки)
            log.debug("📥 Ответ от DeepSeek: {}", mapper.writeValueAsString(response));

            String assistantText = response.choices().get(0).message().content().get();
            // Добавляем ответ ассистента в историю
            ChatCompletionAssistantMessageParam assistantMessageParam = ChatCompletionAssistantMessageParam.builder()
                    .content(assistantText)
                    .build();
            ChatCompletionMessageParam assistantMsg = ChatCompletionMessageParam.ofAssistant(assistantMessageParam);
            messages.add(assistantMsg);

            return assistantText;
        } catch (Exception ex) {
            log.error("Ошибка при запросе DeepSeek API: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Ошибка DeepSeek API: " + ex.getMessage(), ex);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("✅ Завершено за {} ms", elapsed);
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
            - `deleteAssistantHistory` — удалить историю сообщений ассистента

            ---
            🧩 Поведение в сложных ситуациях:

            - Если запрос неполный или неясный — задай уточняющий вопрос.
            - Если команда выполнена и получен ответ — проанализируй и ответь голосом.
            - Если делать ничего не нужно — просто ответь голосом, не вызывая команды.
            """;
    }
}
