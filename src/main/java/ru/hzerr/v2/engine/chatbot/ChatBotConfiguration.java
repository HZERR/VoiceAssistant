package ru.hzerr.v2.engine.chatbot;

public class ChatBotConfiguration {

    private ChatBotType type;
    // ...

    private ChatBotConfiguration() {
    }

    public void setType(ChatBotType type) {
        this.type = type;
    }

    public ChatBotType getType() {
        return type;
    }

    public static ChatBotConfigurationBuilder builder() {
        return new ChatBotConfigurationBuilder();
    }

    public static class ChatBotConfigurationBuilder {

        private ChatBotType type;

        private ChatBotConfigurationBuilder() {
        }

        public ChatBotConfigurationBuilder type(ChatBotType type) {
            this.type = type;
            return this;
        }

        public ChatBotConfiguration build() {
            ChatBotConfiguration configuration = new ChatBotConfiguration();
            configuration.setType(type);
            return configuration;
        }
    }
}
