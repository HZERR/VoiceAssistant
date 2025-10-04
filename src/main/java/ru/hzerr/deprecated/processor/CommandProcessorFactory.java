package ru.hzerr.deprecated.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import ru.hzerr.utils.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandProcessorFactory {

    private static final Logger log = LoggerFactory.getLogger(CommandProcessorFactory.class);
    private final Map<String, IProcessor> processors = new HashMap<>();

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

    public boolean isSupported(String command) {
        return JsonUtils.isJson(command, AssistantCommand[].class);
    }

    public String process(List<AssistantCommand> commands) {
        StringBuilder result = new StringBuilder();

        for (AssistantCommand assistantCommand : commands) {
            IProcessor processor = processors.get(assistantCommand.getCommand());

            if (processor != null) {
                result.append(processor.process(assistantCommand)).append("\n");
            } else {
                log.warn("🔴 Неизвестная команда: {}", assistantCommand.getCommand());
                result.append("Неизвестная команда: ").append(assistantCommand.getCommand()).append("\n");
            }
        }

        return result.toString().trim();
    }
}
