package dron;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 06.09.2016.
 */
public class Session {
    private String sessionName;
    private List<Word> words;

    public Session(String name) {
        sessionName = name;
        words = new ArrayList<>();
    }

    public void addWord(Word word){
        words.add(word);
    }

    public void print(){
        System.out.println(sessionName);
        System.out.println(words);
    }

    public void save() {
        File file = new File(App.testDirectory, sessionName);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            try (PrintWriter out = new PrintWriter(file.getAbsoluteFile())) {
                out.println(sessionName);
                for (Word w : words) {
                    out.println(w);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
