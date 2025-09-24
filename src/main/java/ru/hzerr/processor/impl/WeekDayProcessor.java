package ru.hzerr.processor.impl;

import ru.hzerr.processor.AssistantCommand;
import ru.hzerr.processor.Command;
import ru.hzerr.processor.IProcessor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Command("getCurrentWeekDay")
public class WeekDayProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        return "[КОМПЬЮТЕР] Сегодня: " + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ru"))).toUpperCase();
    }
}
