package ru.dron.activevocabe.model;

import java.util.List;
import java.util.Set;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizResult {
    public final List<Word> testWords;
    public final Set<Word> errors;
    public final QuizProperties properties;

    public QuizResult(List<Word> testWords, Set<Word> errors, QuizProperties properties){
        this.errors = errors;
        this.properties = properties;
        this.testWords = testWords;
    }
}
