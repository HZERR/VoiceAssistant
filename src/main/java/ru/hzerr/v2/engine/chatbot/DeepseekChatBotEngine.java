package ru.hzerr.v2.engine.chatbot;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.hzerr.configuration.ReadOnlyApplicationConfiguration;
import ru.hzerr.utils.JsonUtils;
import ru.hzerr.v2.exception.ProcessingException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public non-sealed class DeepseekChatBotEngine extends BaseChatBotEngine<String> {

    private static final Logger log = LoggerFactory.getLogger(DeepseekChatBotEngine.class);
    private static final List<ChatCompletionMessageParam> messages = Collections.synchronizedList(new ArrayList<>());
    private final ReadOnlyApplicationConfiguration applicationConfiguration;
    private OpenAIClient openAiClient;

    @Autowired
    public DeepseekChatBotEngine(ReadOnlyApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    protected String getModuleName() {
        return "Deepseek";
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
    protected String onProcess(ChatBotProcessingConfiguration processingConfiguration) throws Exception {
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

        ChatCompletion response = openAiClient.chat().completions().create(newMessageParameters);
        log.debug("📥 Получен ответ от ассистента: {}", JsonUtils.writeValueAsString(response));

        if (response.choices().getFirst().message().content().isPresent()) {
            String assistantText = response.choices().getFirst().message().content().get();

            ChatCompletionMessageParam assistantParameters = ChatCompletionMessageParam.ofAssistant(ChatCompletionAssistantMessageParam.builder()
                    .content(assistantText)
                    .build());
            messages.add(assistantParameters);
            log.debug("💾 Ответ ассистента добавлен в историю");

            return assistantText;
        }

        throw new ProcessingException("❌ Пустой ответ от ассистента");
    }

    @Override
    protected void onDestroy() throws Exception {
        openAiClient.close();
    }
}
