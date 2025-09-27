package ru.hzerr.processor.impl.translate;

import ru.hzerr.processor.Command;
import ru.hzerr.processor.impl.TranslateProcessor;

@Command("translateEnToRu")
public class EnToRuTranslateProcessor extends TranslateProcessor {

    @Override
    protected String getSourceLang() {
        return "en";
    }

    @Override
    protected String getTargetLang() {
        return "ru";
    }
}
