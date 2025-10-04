package ru.hzerr.deprecated.processor.impl.translate;

import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.impl.TranslateProcessor;

//@Command("translateRuToEn")
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
