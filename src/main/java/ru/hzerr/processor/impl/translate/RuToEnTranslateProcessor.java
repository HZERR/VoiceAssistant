package ru.hzerr.processor.impl.translate;

import ru.hzerr.processor.Command;
import ru.hzerr.processor.impl.TranslateProcessor;

@Command("translateRuToEn")
public class RuToEnTranslateProcessor extends TranslateProcessor {

    @Override
    protected String getSourceLang() {
        return "ru";
    }

    @Override
    protected String getTargetLang() {
        return "en";
    }
}
