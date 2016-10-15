package ru.dron.activevocabe.model;

import javafx.util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 11.09.2016.
 */
public class Sessions {
    //хер знает как это поле лучше назвать
    private Map<String, Set<Word>> vocabe;
    private Set<Word> resentErrors;
    private String workingDirectory = "C:\\projects\\debug";

    public Sessions() {
        resentErrors = Collections.synchronizedSet(new LinkedHashSet<>());
        vocabe = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    public Sessions(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        resentErrors = Collections.synchronizedSet(new LinkedHashSet<>());
        vocabe = Collections.synchronizedMap(new LinkedHashMap<>());

        try {
            List<File> files = Files.walk(Paths.get(workingDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : files) {
                Pair<String, Set<Word>> session = readSessionFromFile(file);
                vocabe.put(session.getKey(), session.getValue());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean contains(Word w) {
        for (Set<Word> set : vocabe.values()) {
            if (set.contains(w)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(String session){
        return vocabe.containsKey(session);
    }

    public Set<Word> get(String key){
        return vocabe.get(key);
    }

    public Collection<Set<Word>> getValues(){
        return vocabe.values();
    }

    public Word getWord(Word w){
        for (Set<Word> set : vocabe.values()) {
            for (Word word : set) {
                if(word.equals(w)){
                    return word;
                }
            }
        }
        System.err.println("Fuck! Word wasn't in session!");
        System.exit(99);
        return null;
    }

    public String getKeyOf(Word w){
        for (String key : vocabe.keySet()) {
            if(vocabe.get(key).contains(w)){
                return key;
            }
        }
        System.err.println("Fuck! Word wasn't in session!");
        System.exit(99);
        return null;
    }

    public void rename(String oldName, String newName){
        Set<Word> words = vocabe.get(oldName);
        vocabe.remove(oldName);
        vocabe.put(newName, words);
    }

    public void add(String session, Word w){
        if (vocabe.containsKey(session)){
            vocabe.get(session).add(w);
        } else{
            Set<Word> set = Collections.synchronizedSet(new LinkedHashSet<>());
            set.add(w);
            vocabe.put(session, set);
        }
    }

    public void add(String session){
        if (!contains(session)){
            vocabe.put(session, new LinkedHashSet<>());
        }
    }

    public Set<String> getKeys(){
        return vocabe.keySet();
    }

    private Pair<String, Set<Word>> readSessionFromFile(File file) {
        String name = "";
        Set<Word> words = Collections.synchronizedSet(new LinkedHashSet<>());
        try {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file.getAbsoluteFile()), "UTF8"))) {
                if ((name = in.readLine()) == null) {
                    System.err.println("BAD SESSION FILE!");
                    System.exit(1);
                }
                String foreign;
                int knowledge;
                String s;
                while (true) {
                    List<String> translations = new ArrayList<>();

                    if ((foreign = in.readLine()) == null) break;
                    if ((s = in.readLine()) == null || !s.equals("[")) break;
                    while ((s = in.readLine()) != null && !s.equals("]")) {
                        translations.add(s);
                    }
                    if ((s = in.readLine()) == null) break;
                    knowledge = Integer.parseInt(s);

                    Word w = new Word(foreign, translations, knowledge);
                    words.add(w);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        return new Pair<>(name, words);
    }
}
