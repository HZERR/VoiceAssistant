package ru.hzerr.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.processor.AssistantCommand;
import ru.hzerr.processor.Command;
import ru.hzerr.processor.IProcessor;
import ru.hzerr.services.time.TimeWorldService;

import java.util.Objects;

@Command("getCurrentZonedDateTime")
public class ZonedDateTimeProcessor implements IProcessor {

    private static final String DEFAULT_TIME_ZONE = "Europe/Moscow";
    private static final Logger log = LoggerFactory.getLogger(ZonedDateTimeProcessor.class);
    private final TimeWorldService worldTimeService;

    public ZonedDateTimeProcessor(TimeWorldService worldTimeService) {
        this.worldTimeService = worldTimeService;
    }

    @Override
    public String process(AssistantCommand command) {
        StringBuilder result = new StringBuilder("[КОМПЬЮТЕР] ");
        if (command.getArgs().isEmpty())
            command.getArgs().addFirst(DEFAULT_TIME_ZONE);

        try {
            for (String timezone: command.getArgs()) {
                log.debug("🟢 Получена тайм-зона: {}", timezone);
                result.append(worldTimeService.getTimeByTimezone(Objects.toString(timezone)).toString()).append("\n");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return result.append("ERROR: ").append(e.getMessage()).toString();
        }

        return result.toString();
    }
}
