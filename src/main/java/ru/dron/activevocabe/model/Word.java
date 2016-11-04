package ru.dron.activevocabe.model;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 06.09.2016.
 */
public class Word {
    public static final String TRANSLATION_SEPARATOR = "; ";

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

    //this constructor removes any extra whitespaces in foreign and translations
    //so it is convenient to call it from raw strings
    public Word(String foreign, String translation) {
        this.foreign = removeExtraWhiteSpaces(foreign);
        this.translations = Arrays.stream(translation.split(TRANSLATION_SEPARATOR.replaceAll("\\s", "")))
                .map(Word::removeExtraWhiteSpaces)
                .filter(s -> !s.equals(""))
                .collect(Collectors.toList());
        knowledge = 0;
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

    public boolean checkAnswerForeign(String answer) {
        return foreign.equals(answer);
    }

    public boolean checkAnswerTranslation(String answer) {
        for (String translation : translations) {
            if (translation.equals(answer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Word) {
            return foreign.equals(((Word) obj).foreign);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return String.join(System.lineSeparator(), foreign, "[",
                StringUtils.join(translations, System.lineSeparator()),
                "]", Integer.toString(knowledge));
    }

    public static String removeExtraWhiteSpaces(String str) {
        if (str.matches("\\s*")) return "";
        return Arrays.stream(str.split("\\s"))
                .filter(s -> !s.equals(""))
                .reduce("", (s1, s2) -> s1 + " " + s2)
                .substring(1);
    }
}
