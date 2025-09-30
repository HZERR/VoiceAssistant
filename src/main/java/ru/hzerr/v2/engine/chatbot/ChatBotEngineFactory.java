package ru.hzerr.v2.engine.chatbot;

import org.springframework.beans.factory.annotation.Autowired;

public class ChatBotEngineFactory {

    private final DeepseekChatBotEngine deepseekChatBotEngine;
    private final DeepseekStreamableChatBotEngine deepseekStreamableChatBotEngine;

    @Autowired
    public ChatBotEngineFactory(DeepseekChatBotEngine deepseekChatBotEngine,
                                DeepseekStreamableChatBotEngine deepseekStreamableChatBotEngine) {

        this.deepseekChatBotEngine = deepseekChatBotEngine;
        this.deepseekStreamableChatBotEngine = deepseekStreamableChatBotEngine;
    }

    public IChatBotEngine getEngine(ChatBotConfiguration chatBotConfiguration) {
        return switch (chatBotConfiguration.getType()) {
            case DEEPSEEK_STREAM -> deepseekStreamableChatBotEngine;
            case DEEPSEEK -> deepseekChatBotEngine;
            default -> throw new IllegalStateException(String.format("❌ Тип ассистента '%s' не поддерживается", chatBotConfiguration.getType()));
        };
    }
}
