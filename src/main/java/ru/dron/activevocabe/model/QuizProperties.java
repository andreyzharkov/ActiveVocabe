package ru.dron.activevocabe.model;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizProperties {
    private String sessionName;
    private int numberOfQuestions;
    private QuizType type;
    private boolean isQuestionsOnForeign;

    public enum QuizType {
        RANDOM,
        RATING,
        SESSION,
        ERRORS
    }

    public QuizProperties(QuizType type, int numberOfQuestions, boolean isQuestionsOnForeign) {
        this.type = type;
        this.numberOfQuestions = numberOfQuestions;
        this.isQuestionsOnForeign = isQuestionsOnForeign;
    }

    public QuizProperties(QuizType type, int numberOfQuestions, boolean isQuestionsOnForeign,
                   String sessionName) {
        this.type = type;
        this.numberOfQuestions = numberOfQuestions;
        this.isQuestionsOnForeign = isQuestionsOnForeign;
        this.sessionName = sessionName;
    }

    public QuizType getQuizType() {
        return type;
    }

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public String getSessionName() {
        return sessionName;
    }

    public boolean isQuestionsOnForeign() {
        return isQuestionsOnForeign;
    }
}
