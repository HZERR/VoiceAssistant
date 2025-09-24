package ru.hzerr.processor.impl;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import ru.hzerr.processor.AssistantCommand;
import ru.hzerr.processor.Command;
import ru.hzerr.processor.IProcessor;

@Command("getCpuInformation")
public class CpuInformationProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = si.getHardware().getProcessor();

        return """
               [КОМПЬЮТЕР]
               💻 Процессор:
               ▪ Модель: %s
               ▪ Ядер: %d (логических: %d)
               ▪ Тактовая частота: %.2f ГГц
               """.formatted(
                processor.getProcessorIdentifier().getName(),
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount(),
                processor.getMaxFreq() / 1_000_000_000.0
        );
    }
}
