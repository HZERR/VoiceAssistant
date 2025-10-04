package ru.hzerr.v2.command.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.v2.exception.ProcessingException;
import ru.hzerr.v2.format.v1.BotAction;

import java.time.ZoneId;
import java.util.Locale;

public abstract class BaseProcessor implements IProcessor {

    private static final Logger log = LoggerFactory.getLogger(BaseProcessor.class);
    protected static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Moscow");
    protected static final Locale RUSSIAN_LOCALE = Locale.forLanguageTag("ru");

    @Override
    @PostConstruct
    public final void init() throws Exception {
        String commandName = getClass().getAnnotation(Command.class).value();
        log.debug("📦 Инициализация команды '{}'...", commandName);
        long startTime = System.currentTimeMillis();
        onInitialize();
        log.debug("✅ Команда '{}' инициализирована системой за {}ms", commandName, System.currentTimeMillis() - startTime);
    }

    @Override
    public final String process(BotAction action) throws ProcessingException {
        log.debug("🧠 Обработка команды началась (name: {}, args: {})...", action.getCommand(), action.getArgs().size());
        long startTime = System.currentTimeMillis();
        try {
            return onProcess(action);
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке команды", e);
            throw new ProcessingException(e.getMessage(), e);
        } finally {
            log.debug("✅ Обработка команды завершена за {}ms", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    @PreDestroy
    public final void destroy() throws Exception {
        String commandName = getClass().getAnnotation(Command.class).value();
        log.debug("📦 Завершение работы команды '{}'...", commandName);
        long startTime = System.currentTimeMillis();
        onDestroy();
        log.debug("✅ Команда '{}' успешно завершена системой за {}ms", commandName, System.currentTimeMillis() - startTime);
    }

    protected abstract void onInitialize() throws Exception;
    protected abstract String onProcess(BotAction action) throws Exception;
    protected abstract void onDestroy() throws Exception;
}
