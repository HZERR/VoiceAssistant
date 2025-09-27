package ru.hzerr.v2.engine.tts;

public class TextToSpeechConfiguration {

    private TextToSpeechEngineType engineType;
    // ...

    private TextToSpeechConfiguration() {
    }

    public void setType(TextToSpeechEngineType engineType) {
        this.engineType = engineType;
    }

    public TextToSpeechEngineType getType() {
        return engineType;
    }

    public static TextToSpeechConfigurationBuilder builder() {
        return new TextToSpeechConfigurationBuilder();
    }

    public static class TextToSpeechConfigurationBuilder {

        private TextToSpeechEngineType type;

        private TextToSpeechConfigurationBuilder() {
        }

        public TextToSpeechConfigurationBuilder type(TextToSpeechEngineType type) {
            this.type = type;
            return this;
        }

        public TextToSpeechConfiguration build() {
            TextToSpeechConfiguration configuration = new TextToSpeechConfiguration();
            configuration.setType(type);
            return configuration;
        }
    }
}
