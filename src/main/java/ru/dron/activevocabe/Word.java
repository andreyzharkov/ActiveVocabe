package ru.dron.activevocabe;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrey on 06.09.2016.
 */
public class Word {
    private String foreign;
    private List<String> translations;
    private int knowledge;

    public Word(String foreign, List<String> translations) {
        this.foreign = foreign;
        this.translations = Collections.synchronizedList(new LinkedList<>(translations));
        knowledge = 0;
    }

    public Word(String foreign, List<String> translations, int knowledge) {
        this.foreign = foreign;
        this.translations = Collections.synchronizedList(new LinkedList<>(translations));
        translations.sort(new java.util.Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return 0;
            }
        });
        this.knowledge = knowledge;
    }

    public String getForeign() {
        return foreign;
    }

    public List<String> getTranslations() {
        return translations;
    }

    public static Comparator<Word> getKnowledgeComparator() {
        return (o1, o2) -> o1.knowledge - o2.knowledge;
    }

    public void updateKnowledge(boolean wasAnswerCorrect) {
        if (wasAnswerCorrect) {
            knowledge++;
        } else {
            knowledge = 0;
        }
    }

    public boolean checkAnswerForeign(String answer){
        return foreign.equals(answer);
    }

    public boolean checkAnswerTranslation(String answer){
        for (String translation: translations){
            if (translation.equals(answer)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Word){
            return foreign.equals(((Word)obj).foreign);
        }
        return super.equals(obj);
    }

    @Override
    public String toString(){
        return String.join(System.lineSeparator(), foreign, "[",
                StringUtils.join(translations, System.lineSeparator()),
                "]", Integer.toString(knowledge));
    }
}
