package ru.hzerr.v2.command.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.v2.command.processor.BaseProcessor;
import ru.hzerr.v2.exception.ProcessingException;
import ru.hzerr.v2.format.v1.BotAction;

import java.net.*;
import java.time.Instant;
import java.time.ZoneId;

@Command("getCurrentZonedDateTime")
public class ZonedDateTimeProcessor extends BaseProcessor {

    private static final String DEFAULT_TIME_ZONE = "Europe/Moscow";
    private static final Logger log = LoggerFactory.getLogger(ZonedDateTimeProcessor.class);

    @Override
    protected void onInitialize() throws Exception {
    }

    @Override
    protected String onProcess(BotAction action) throws Exception {
        if (action.getArgs().isEmpty())
            action.getArgs().addFirst(DEFAULT_TIME_ZONE);

        Instant currentInstant = getCurrentInstant();

        StringBuilder result = new StringBuilder();
        for (String timezone : action.getArgs()) {
            log.debug("🟢 Получена тайм-зона: {}", timezone);

            try {
                ZoneId zoneId = ZoneId.of(timezone);
                result.append(timezone).append(": ").append(currentInstant.atZone(zoneId)).append("\n");
            } catch (Exception e) {
                result.append(timezone).append(": ").append(e.getMessage()).append("\n");
            }
        }

        return result.toString();
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

    @Override
    protected void onDestroy() throws Exception {
    }
}
