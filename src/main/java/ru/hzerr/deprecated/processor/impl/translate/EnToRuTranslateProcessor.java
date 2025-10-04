package ru.hzerr.deprecated.processor.impl.translate;

import ru.hzerr.deprecated.processor.Command;
import ru.hzerr.deprecated.processor.impl.TranslateProcessor;

//@Command("translateEnToRu")
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
