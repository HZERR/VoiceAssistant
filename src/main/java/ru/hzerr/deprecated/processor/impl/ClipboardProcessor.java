package ru.hzerr.deprecated.processor.impl;

import ru.hzerr.deprecated.processor.AssistantCommand;
import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.IProcessor;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;

//@Command("getClipboard")
public class ClipboardProcessor implements IProcessor {

    @Override
    public String process(AssistantCommand command) {
        try {
            return "[БУФЕР ОБМЕНА]" + Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            return "[ОШИБКА] Не удалось получить текст из буфера обмена: " + e.getMessage();
        }
    }
}
