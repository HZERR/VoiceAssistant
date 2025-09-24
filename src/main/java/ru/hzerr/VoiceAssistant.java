package ru.hzerr;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hzerr.configuration.SpringConfiguration;

public class VoiceAssistant {
    private static final Logger log = LoggerFactory.getLogger(VoiceAssistant.class);

    public static void main(String[] args) throws Exception {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        long startTime = System.currentTimeMillis();
        GlobalScreen.registerNativeHook();
        log.debug("⚙️ Регистрация хуков завершена за {}ms", System.currentTimeMillis() - startTime);
        GlobalScreen.addNativeKeyListener(context.getBean(NativeKeyListener.class));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (Exception ignored) {}

            context.close();
        }));

        log.debug("🎙️ Нажми F7, чтобы включить/выключить запись. F8 для выхода.");
    }
}