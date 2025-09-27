package ru.hzerr.v2.engine.record;

public class SpeechRecordConfiguration {

    private String deviceName;
    // ...

    private SpeechRecordConfiguration() {
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public static SpeechRecordConfigurationBuilder builder() {
        return new SpeechRecordConfigurationBuilder();
    }

    public static class SpeechRecordConfigurationBuilder {

        private String deviceName;

        private SpeechRecordConfigurationBuilder() {
        }

        public SpeechRecordConfigurationBuilder deviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public SpeechRecordConfiguration build() {
            SpeechRecordConfiguration configuration = new SpeechRecordConfiguration();
            configuration.setDeviceName(deviceName);
            return configuration;
        }
    }
}
