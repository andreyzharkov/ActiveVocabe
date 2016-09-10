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

    public Session(File file, int id) {
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
                    Word w = new Word(eng, ru, knowledge);
                    w.setSessionId(id);
                    words.add(w);
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

    public void setId(int id) {
        words.forEach(w -> setId(id));
    }

    public void updateWordKnowledge(int wordIndex, boolean correct){
        Word w = words.get(wordIndex);
        words.remove(wordIndex);
        w.updateKnowledge(correct);
        words.add(wordIndex, w);
    }
}
