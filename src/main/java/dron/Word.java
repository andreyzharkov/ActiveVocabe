package dron;

/**
 * Created by Andrey on 06.09.2016.
 */
public class Word {
    public Word(String foreign, String original) {
        this.foreign = foreign;
        this.original = original;
        knowledge = 0;
    }

    public Word(String foreign, String original, int knowledge) {
        this.foreign = foreign;
        this.original = original;
        this.knowledge = knowledge;
    }

    private String foreign;
    private String original;
    private int knowledge;
    private int sessionId;

    public final String getForeign() {
        return foreign;
    }

    public final String getOriginal() {
        return original;
    }

    public final int getSessionId() {
        return sessionId;
    }

    public final void setSessionId(int id) {
        sessionId = id;
    }

    public final void updateKnowledge(boolean answer){
        if (answer) knowledge++;
        else knowledge--;
    }

    @Override
    public String toString() {
        //Довольно примитивно, но с парсером вломень возиться
        return String.join(System.lineSeparator(), foreign, original,
                Integer.toString(knowledge));
    }
}
