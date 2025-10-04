package ru.hzerr.deprecated.processor.impl;

import ru.hzerr.deprecated.processor.AssistantCommand;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.IProcessor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

//@Command("getCurrentWeekDay")
public class WeekDayProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        return "[КОМПЬЮТЕР] Сегодня: " + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ru"))).toUpperCase();
    }
}
