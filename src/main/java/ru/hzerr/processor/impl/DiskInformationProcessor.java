package ru.hzerr.processor.impl;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import ru.hzerr.processor.AssistantCommand;
import ru.hzerr.processor.Command;
import ru.hzerr.processor.IProcessor;

import java.util.List;

@Command("getDiskInformation")
public class DiskInformationProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        SystemInfo si = new SystemInfo();
        List<HWDiskStore> disks = si.getHardware().getDiskStores();

        StringBuilder sb = new StringBuilder("💽 Диски:\n");

        for (HWDiskStore disk : disks) {
            sb.append("""
                     [КОМПЬЮТЕР]
                     ▪ Название: %s
                       ▪ Модель: %s
                       ▪ Размер: %.2f GB
                       ▪ Считываний: %d
                       ▪ Записей: %d
                     """.formatted(
                    RegExUtils.removeFirst(disk.getName(), "^\\\\.\\\\"),
                    disk.getModel(),
                    disk.getSize() / 1e9,
                    disk.getReads(),
                    disk.getWrites()
            ));
        }

        return sb.toString();
    }
}

