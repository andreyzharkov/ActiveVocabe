package ru.dron.activevocabe.model;

import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;

import static ru.dron.activevocabe.model.Word.TRANSLATION_SEPARATOR;

/**
 * Created by Andrey on 04.11.2016.
 */
public class TWord {
    private final SimpleStringProperty foreign;
    private final SimpleStringProperty translation;

    public TWord(Word w) {
        this.foreign = new SimpleStringProperty(w.getForeign());
        this.translation = new SimpleStringProperty(StringUtils
                .join(w.getTranslations(), TRANSLATION_SEPARATOR));
    }

    public Word getWord() {
        return new Word(getForeign(), getTranslation());
    }

    public String getForeign() {
        return foreign.get();
    }

    public void setForeign(String foreign_) {
        foreign.set(foreign_);
    }

    public String getTranslation() {
        return translation.get();
    }

    public void setTranslation(String tr) {
        translation.set(tr);
    }
}
