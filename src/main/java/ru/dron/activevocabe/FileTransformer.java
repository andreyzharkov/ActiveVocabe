package ru.dron.activevocabe;

import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 23.10.2016.
 */
//осталось добавить возможность выбрать кодировку файла
//и нормально считывать \ -> \, а на \ -> \\
public class FileTransformer {
    public static FileTransformer getInstance() {
        if (instance == null) {
            instance = new FileTransformer();
        }
        return instance;
    }

    private static FileTransformer instance;

    Set<String> punctuationCharacters = new HashSet<>(Arrays.asList(". , ; :".split(" ")));

    String pathToOriginalFile;
    String sessionName;

    //separators are regexps!!!
    //like "(\\.|,|;)"
    String ftSeparator;
    String trSeparator;
    boolean numberRemoved;
    boolean bracketsRemoved;
    boolean punctuationRemoved;

    public FileTransformer() {

    }

    public void setProperties(String filePath, String session, String ftSeparator, String trSeparator,
                              boolean numberRemoved, boolean bracketsRemoved, boolean punctuationRemoved) {
        pathToOriginalFile = filePath;
        sessionName = session;
        this.ftSeparator = ftSeparator;
        this.trSeparator = trSeparator;
        this.numberRemoved = numberRemoved;
        this.bracketsRemoved = bracketsRemoved;
        this.punctuationRemoved = punctuationRemoved;
        punctuationCharacters.removeIf(s -> s.matches(ftSeparator));
        punctuationCharacters.removeIf(s -> s.matches(trSeparator));
    }

    public Set<Word> readFile() {
        if (pathToOriginalFile == null) {
            return null;
        }
        String line;
        Set<Word> words = new HashSet<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(pathToOriginalFile), "cp1251"))) {
            while ((line = in.readLine()) != null) {
                words.add(lineToWord(line));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(5);
        }
        return words;
    }

    private static Word stringToWord(String str) {
        System.out.println(str);
        str = StringUtils.join(str.split("\\d"));
        //System.out.println(str);
        str = StringUtils.join(Arrays.stream(str.split("\\s")).filter(s -> !s.equals("")).collect(Collectors.toList()), " ");
        //System.out.println(str);
        str = str.replaceAll("\\[\\S*]", "");
        str = str.replaceAll("\\([^1]*\\)", "");
        str = str.replaceAll("\\)", "");
        str = str.replaceAll("\\S*\\.", "");
        //System.out.println(str);
        String foreign;
        LinkedList<String> arr = new LinkedList<>(Arrays.asList(str.split("(a-|n-|adv-|cj-|int-|n-|part-|prep-|pron-|v-|~s)")));
        //System.out.println(str.split("(n-|adv-|cj-|int-|n-|part-|prep-|pron-|v-|~s)")[0]);
        foreign = arr.get(0);
        arr.remove(0);
        //System.out.println(arr.get(0));
        //System.out.println(Arrays.stream(arr.get(0).split("(,|;|\\.)")).collect(Collectors.toList()));
        List<String> translations = arr.stream().map(s -> s.split("(;|\\.|,)"))
                .flatMap(Arrays::stream)
                .map(s -> s.replaceAll("(^\\s*|\\s*$|\\([^1]*\\))", ""))
                .filter(s -> !s.equals(""))
                .collect(Collectors.toList());

        System.out.println("foreign=" + foreign);
        System.out.println(translations);
        return new Word(foreign, translations);
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

        System.out.println("foreign=" + foreign);
        System.out.println(translations);

        return new Word(foreign, translations);
    }

    public static void main() {
        //FileTransformer transformer = new FileTransformer();
        String filename = "C:\\projects\\debug\\yy.txt";
        File file = new File(filename);

        //ужасный костыль под винду
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(file.getAbsoluteFile()), "cp1251"))) {
            String s;
            while ((s = in.readLine()) != null) {
                stringToWord(s);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception");
        }
    }
}
