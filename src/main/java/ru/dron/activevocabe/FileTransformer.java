package ru.dron.activevocabe;

import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.Word;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 23.10.2016.
 */
public class FileTransformer {
    public static FileTransformer getInstance() {
        if (instance == null) {
            instance = new FileTransformer();
        }
        return instance;
    }

    private static FileTransformer instance;

    private Set<String> punctuationCharacters = new HashSet<>(Arrays.asList(". , ; : ! ?".split(" ")));

    private String pathToOriginalFile;

    //separators are regexps!!!
    //like "(\\.|,|;)"
    private String ftSeparator;
    private String trSeparator;
    private boolean numberRemoved;
    private boolean bracketsRemoved;
    private boolean punctuationRemoved;
    private String fileEncoding;

    public FileTransformer() {

    }

    public void setProperties(String filePath, String ftSeparator, String trSeparator,
                              boolean numberRemoved, boolean bracketsRemoved, boolean punctuationRemoved,
                              String encoding) {
        pathToOriginalFile = filePath;
        this.ftSeparator = ftSeparator.replaceAll("\\\\\\\\", "\\\\");
        this.trSeparator = trSeparator.replaceAll("\\\\\\\\", "\\\\");
        this.numberRemoved = numberRemoved;
        this.bracketsRemoved = bracketsRemoved;
        this.punctuationRemoved = punctuationRemoved;
        punctuationCharacters.removeIf(s -> s.matches(ftSeparator));
        punctuationCharacters.removeIf(s -> s.matches(trSeparator));
        fileEncoding = encoding;
    }

    public Set<Word> readFile() {
        if (pathToOriginalFile == null) {
            return null;
        }
        String line;
        Set<Word> words = new HashSet<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(pathToOriginalFile), fileEncoding))) {
            while ((line = in.readLine()) != null) {
                words.add(lineToWord(line));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(5);
        }
        return words;
    }

    private Word lineToWord(String line) {
        if (numberRemoved) {
            line = StringUtils.join(line.split("\\d"));
        }
        if (bracketsRemoved) {
            line = line.replaceAll("\\[[^]]*]", "");
            line = line.replaceAll("\\([^)]*\\)", "");
            line = line.replaceAll("(\\)|\\[|]|\\()", "");
        }
        if (punctuationRemoved) {
            if (punctuationCharacters.contains(".")) {
                punctuationCharacters.remove(".");
                punctuationCharacters.add("\\.");
            }
            line = line.replaceAll("(" + punctuationCharacters.stream().reduce((s1, s2) -> s1 + "|" + s2) + ")", "");
        }

        line = StringUtils.join(Arrays.stream(line.split("\\s")).filter(s -> !s.equals("")).collect(Collectors.toList()), " ");

        String foreign;
        LinkedList<String> arr = new LinkedList<>(Arrays.asList(line.split(ftSeparator)));

        foreign = arr.get(0);
        arr.remove(0);
        line = StringUtils.join(arr, " ");

        List<String> translations = Arrays.stream(line.split(trSeparator))
                .map(s -> s.replaceAll("(^\\s*|\\s*$)", ""))
                .filter(s -> !s.equals(""))
                .collect(Collectors.toList());

        return new Word(foreign, translations);
    }
}
