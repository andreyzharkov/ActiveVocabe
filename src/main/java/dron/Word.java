package dron;

/**
 * Created by Andrey on 06.09.2016.
 */
public class Word {
    public Word(String foreign, String original, String hint_foreign, String hint_original) {
        this.foreign = foreign;
        this.original = original;
        this.hint_foreign = hint_foreign;
        this.hint_original = hint_original;
        knowledge = 0;
    }

    public Word(String foreign, String original, String hint_foreign, String hint_original, int knowledge) {
        this.foreign = foreign;
        this.original = original;
        this.hint_foreign = hint_foreign;
        this.hint_original = hint_original;
        this.knowledge = knowledge;
    }

    private String foreign;
    private String original;
    private String hint_foreign;
    private String hint_original;
    private int knowledge;

    public final String getForeign() {
        return foreign;
    }

    public final String getOriginal() {
        return original;
    }

    public final String getHint_foreign() {
        return hint_foreign;
    }

    public final String getHint_original() {
        return hint_original;
    }

    @Override
    public String toString() {
        //Довольно примитивно, но с парсером вломень возиться
        return String.join(System.lineSeparator(), foreign, original,
                hint_foreign, hint_original, Integer.toString(knowledge));
    }
}
