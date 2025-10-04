package ru.hzerr.v2.command.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.v2.command.processor.BaseProcessor;
import ru.hzerr.v2.exception.ProcessingException;
import ru.hzerr.v2.format.v1.BotAction;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Command("getCurrentWeekDay")
public class WeekDayProcessor extends BaseProcessor {

    private static final DateTimeFormatter DAY_OF_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE", RUSSIAN_LOCALE);
    private static final Logger log = LoggerFactory.getLogger(WeekDayProcessor.class);

    @Override
    protected void onInitialize() throws Exception {
    }

    @Override
    protected String onProcess(BotAction action) throws Exception {
        return getCurrentInstant().atZone(DEFAULT_ZONE_ID).format(DAY_OF_WEEK_FORMATTER);
    }

    @Override
    protected void onDestroy() throws Exception {
    }

    private Instant getCurrentInstant() throws ProcessingException {
        String[] addresses = {
                "https://www.google.com",
                "https://www.microsoft.com",
                "https://www.apple.com",
                "https://www.amazon.com"
        };

        for (String address : addresses) {
            try {
                Instant time = getInstantFromURL(address);
                if (time != null) return time;
            } catch (Exception e) {
                log.warn("Не удалось получить время из {}: {}", address, e.getMessage());
            }
        }

        throw new ProcessingException("Сервера не вернули время");
    }

    private Instant getInstantFromURL(String address) throws Exception {
        URL url = new URI(address).toURL();
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        connection.connect();

        long serverTime = connection.getHeaderFieldDate("Date", 0);
        return serverTime != 0 ? Instant.ofEpochMilli(serverTime) : null;
    }
}
