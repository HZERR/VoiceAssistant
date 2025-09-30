package ru.hzerr.v2.engine.play;

public class AudioPlaybackConfiguration {

    private AudioPlaybackConfiguration() {
    }

    public static AudioPlaybackConfigurationBuilder builder() {
        return new AudioPlaybackConfigurationBuilder();
    }

    public static class AudioPlaybackConfigurationBuilder {

        private AudioPlaybackConfigurationBuilder() {
        }

        public AudioPlaybackConfiguration build() {
            AudioPlaybackConfiguration configuration = new AudioPlaybackConfiguration();
            return configuration;
        }
    }
}
