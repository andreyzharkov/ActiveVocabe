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

    public final String getForeign() {
        return foreign;
    }

    public final String getOriginal() {
        return original;
    }

    @Override
    public String toString() {
        //Довольно примитивно, но с парсером вломень возиться
        return String.join(System.lineSeparator(), foreign, original,
                Integer.toString(knowledge));
    }
}
