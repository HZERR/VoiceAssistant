package ru.hzerr.deprecated.processor.impl;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import ru.hzerr.deprecated.processor.AssistantCommand;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.IProcessor;

//@Command("getMemoryInformation")
public class MemoryInformationProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();

        long total = memory.getTotal();
        long available = memory.getAvailable();

        return """
               [КОМПЬЮТЕР]
               🧠 Оперативная память:
               ▪ Всего: %.2f GB
               ▪ Доступно: %.2f GB
               ▪ Используется: %.2f GB
               """.formatted(
                total / 1e9,
                available / 1e9,
                (total - available) / 1e9
        );
    }
}

