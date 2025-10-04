package ru.hzerr.deprecated.processor.impl;

import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;
import ru.hzerr.deprecated.processor.AssistantCommand;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.IProcessor;

//@Command("getOperatingSystemInformation")
public class OperatingSystemInformationProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();

        return """
               [КОМПЬЮТЕР]
               📦 Операционная система:
               ▪ Название: %s
               ▪ Версия: %s
               ▪ Архитектура: %s
               """.formatted(
                os.toString(),
                os.getVersionInfo().getVersion(),
                System.getProperty("os.arch")
        );
    }
}

