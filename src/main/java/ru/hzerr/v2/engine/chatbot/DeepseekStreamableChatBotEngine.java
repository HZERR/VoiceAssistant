package ru.hzerr.v2.engine.chatbot;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public non-sealed class DeepseekStreamableChatBotEngine extends BaseChatBotEngine<Flux<ChatCompletionChunk>> {

    private static final Logger log = LoggerFactory.getLogger(DeepseekStreamableChatBotEngine.class);
    private static final List<ChatCompletionMessageParam> messages = Collections.synchronizedList(new ArrayList<>());
    private final ReadOnlyApplicationConfiguration applicationConfiguration;
    private OpenAIClient openAiClient;

    @Autowired
    public DeepseekStreamableChatBotEngine(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    protected String getModuleName() {
        return "Deepseek Stream";
    }

    @Override
    protected void onInitialize() throws Exception {
        this.openAiClient = OpenAIOkHttpClient.builder()
                .apiKey(applicationConfiguration.getOpenAIAssistantApiKey())
                .baseUrl(applicationConfiguration.getOpenAIAssistantBaseUrl())
                .timeout(Duration.ofSeconds(30))
                .build();

        ChatCompletionSystemMessageParam systemPromptMessage = ChatCompletionSystemMessageParam.builder()
                .content(getInstructions())
                .build();

        messages.add(ChatCompletionMessageParam.ofSystem(systemPromptMessage));
    }

    @Override
    protected Flux<ChatCompletionChunk> onProcess(ChatBotProcessingConfiguration processingConfiguration) throws Exception {
        ChatCompletionMessageParam newMessage = switch (processingConfiguration.getRole()) {
            case USER -> ChatCompletionMessageParam.ofUser(ChatCompletionUserMessageParam.builder().content(processingConfiguration.getMessage()).build());
            case TOOLS -> ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder().content(processingConfiguration.getMessage()).build());
            case DEVELOPER -> ChatCompletionMessageParam.ofDeveloper(ChatCompletionDeveloperMessageParam.builder().content(processingConfiguration.getMessage()).build());
            case SYSTEM -> ChatCompletionMessageParam.ofSystem(ChatCompletionSystemMessageParam.builder().content(processingConfiguration.getMessage()).build());
            case ASSISTANT -> ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder().content(processingConfiguration.getMessage()).build());
        };

        messages.add(newMessage);

        ChatCompletionCreateParams newMessageParameters = ChatCompletionCreateParams.builder()
                .model("deepseek-chat")
                .messages(messages)
                .temperature(0.7)
                .topP(1.0)
                .build();

        StringBuilder responseBuilder = new StringBuilder();
        return Flux.using(
                        () -> openAiClient.chat().completions().createStreaming(newMessageParameters),
                        response -> Flux.fromStream(response.stream()),
                        StreamResponse::close
                )
                .doOnSubscribe(_ -> log.debug("▶️ Потоковая обработка ассистента начата"))
                .doOnNext(_ -> log.debug("📥 Получен фрагмент ответа от ассистента"))
                .doOnNext(chunk -> {
                    if (!chunk.choices().isEmpty()) {
                        responseBuilder.append(chunk.choices().getFirst().delta().content());
                    }
                })
                .doOnComplete(() -> {
                    log.debug("✅ Потоковая обработка ассистента завершена");
                    String response = responseBuilder.toString();
                    if (StringUtils.isNotEmpty(response)) {
                        ChatCompletionMessageParam assistantMessage = ChatCompletionMessageParam.ofAssistant(
                                ChatCompletionAssistantMessageParam.builder()
                                        .content(response)
                                        .build()
                        );
                        messages.add(assistantMessage);
                        log.debug("💾 Ответ ассистента добавлен в историю");
                    }
                })
                .doOnError(error -> log.error("❌ Потоковая обработка ассистента завершилась с ошибкой", error));
    }

    @Override
    protected void onDestroy() throws Exception {
        openAiClient.close();
    }
}
