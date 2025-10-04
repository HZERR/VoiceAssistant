package ru.hzerr.v2.command.processor.impl;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.v2.command.processor.BaseProcessor;
import ru.hzerr.v2.format.v1.BotAction;

import java.util.List;

@Command("getSystemInformation")
public class SystemInformationProcessor extends BaseProcessor {

    @Override
    protected void onInitialize() throws Exception {

    }

    @Override
    protected String onProcess(BotAction action) throws Exception {
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        HardwareAbstractionLayer hardware = si.getHardware();
        CentralProcessor cpu = hardware.getProcessor();
        GlobalMemory memory = hardware.getMemory();
        Sensors sensors = hardware.getSensors();

        // CPU
        double systemLoad = cpu.getSystemLoadAverage(3)[0];
        String loadText = systemLoad >= 0 ? String.format("load: %.1f", systemLoad) : "load: N/A";
        String cpuInfo = String.format("CPU: %s (%d/%d ядер, %.2f ГГц, %s, %s)",
                cpu.getProcessorIdentifier().getName().split("@")[0].trim(),
                cpu.getPhysicalProcessorCount(),
                cpu.getLogicalProcessorCount(),
                cpu.getMaxFreq() / 1E9D,
                System.getProperty("os.arch"),
                loadText);

        // Память
        long totalMem = memory.getTotal();
        long availableMem = memory.getAvailable();
        String memoryInfo = String.format("RAM: %.1f/%.1f GB (%.0f%%)",
                (totalMem - availableMem) / 1E9, totalMem / 1E9,
                (totalMem - availableMem) * 100D / totalMem);

        // Диски
        List<OSFileStore> fileStores = os.getFileSystem().getFileStores();
        StringBuilder diskInfo = new StringBuilder("Disk:");
        for (int i = 0; i < fileStores.size(); i++) {
            OSFileStore store = fileStores.get(i);
            long total = store.getTotalSpace();
            long used = total - store.getFreeSpace();
            int percent = total > 0 ? (int) ((used * 100D) / total) : 0;
            diskInfo.append(String.format(" %d(%.1f/%.1fGB %d%%)",
                    i + 1, used / 1E9, total / 1E9, percent));
        }

        // Дополнительная информация
        long swapTotal = memory.getVirtualMemory().getSwapTotal();
        String swapInfo = swapTotal > 0 ?
                String.format(" | Swap: %.1f/%.1fGB",
                        memory.getVirtualMemory().getSwapUsed() / 1E9, swapTotal / 1E9) : "";

        double cpuTemp = sensors.getCpuTemperature();
        String sensorsInfo = cpuTemp > 0 ? String.format(" | %.1f°C", cpuTemp) : "";

        long uptime = os.getSystemUptime();
        String uptimeInfo = String.format(" | Uptime: %dч %dм", uptime / 3600, (uptime % 3600) / 60);

        // ОС
        String bitness = os.getBitness() + "-bit";
        String osInfo = String.format("OS: %s %s (%s)", os.getFamily(), os.getVersionInfo().getVersion(), bitness);

        return String.format("%s | %s | %s | %s%s%s%s", cpuInfo, memoryInfo, diskInfo, osInfo, swapInfo, sensorsInfo, uptimeInfo);
    }

    @Override
    protected void onDestroy() throws Exception {

    }
}
