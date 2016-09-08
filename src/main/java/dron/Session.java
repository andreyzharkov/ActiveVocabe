package dron;

import java.io.*;
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

    public Session(File file) {
        words = new ArrayList<>();
        try {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file.getAbsoluteFile()), "UTF8"))) {
                if ((sessionName = in.readLine()) == null) System.err.println("BAD SESSION FILE!");
                String ru, eng;
                int knowledge;
                String s;
                while (true) {
                    if ((eng = in.readLine()) == null) break;
                    if ((ru = in.readLine()) == null) break;
                    if ((s = in.readLine()) == null) break;
                    knowledge = Integer.parseInt(s);
                    words.add(new Word(eng, ru, knowledge));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWord(Word word) {
        words.add(word);
    }

    public void print() {
        System.out.println(sessionName);
        System.out.println(words);
    }

    public int getLength() {
        return words.size();
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

    public List<Word> getWords() {
        return words;
    }

    public String getName() {
        return sessionName;
    }
}
