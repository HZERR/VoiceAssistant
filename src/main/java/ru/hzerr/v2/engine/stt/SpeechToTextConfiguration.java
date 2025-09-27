package ru.hzerr.v2.engine.stt;

public class SpeechToTextConfiguration {

    private SpeechToTextEngineType type;
    // ...

    private SpeechToTextConfiguration() {
    }

    public void setType(SpeechToTextEngineType engineType) {
        this.type = engineType;
    }

    public SpeechToTextEngineType getType() {
        return type;
    }

    public static SpeechToTextConfigurationBuilder builder() {
        return new SpeechToTextConfigurationBuilder();
    }

    public static class SpeechToTextConfigurationBuilder {

        private SpeechToTextEngineType type;

        private SpeechToTextConfigurationBuilder() {
        }

        public SpeechToTextConfigurationBuilder type(SpeechToTextEngineType type) {
            this.type = type;
            return this;
        }

        public SpeechToTextConfiguration build() {
            SpeechToTextConfiguration configuration = new SpeechToTextConfiguration();
            configuration.setType(type);
            return configuration;
        }
    }
}
