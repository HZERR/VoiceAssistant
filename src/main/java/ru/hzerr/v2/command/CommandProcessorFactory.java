package ru.hzerr.v2.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.v2.command.processor.IProcessor;
import ru.hzerr.v2.exception.ProcessingException;
import ru.hzerr.v2.format.v1.BotAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandProcessorFactory {

    private static final Logger log = LoggerFactory.getLogger(CommandProcessorFactory.class);
    private static final Map<String, IProcessor> processors = new HashMap<>();

    @Autowired
    public CommandProcessorFactory(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Command.class);

        for (Object bean : beans.values()) {
            if (bean instanceof IProcessor processor) {
                Command annotation = bean.getClass().getAnnotation(Command.class);
                String commandName = annotation.value();
                processors.put(commandName, processor);
                log.debug("✅ Зарегистрирована команда: {}", commandName);
            }
        }
    }

    public String process(List<BotAction> actions) {
        StringBuilder result = new StringBuilder();

        for (BotAction action : actions) {
            IProcessor processor = processors.get(action.getCommand());

            if (processor != null) {
                try {
                    result.append(processor.process(action)).append("\n");
                } catch (ProcessingException pe) {
                    result.append("Команда '").append(action.getCommand()).append("' не была обработана. ").append(pe.getClass().getSimpleName()).append(": ").append(pe.getMessage()).append("\n");
                }
            } else {
                log.warn("🔴 Неизвестная команда: {}", action.getCommand());
                result.append("Неизвестная команда: ").append(action.getCommand()).append("\n");
            }
        }

        return result.toString().trim();
    }
}
