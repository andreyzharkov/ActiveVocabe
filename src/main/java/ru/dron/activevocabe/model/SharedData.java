package ru.dron.activevocabe.model;

import javafx.scene.control.TreeView;
import ru.dron.activevocabe.controllers.RootPaneController;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Andrey on 15.10.2016.
 */
public class SharedData {
    private final String rootDirectory;
    private Sessions sessions;
    private Set<Word> resentErrors;
    private TreeView<String> treeView;

    public SharedData(TreeView<String> treeView) {
        this.treeView = treeView;
        rootDirectory = System.getProperty("user.dir") + File.separator + "root";
        File checker = new File(rootDirectory);
        if (!checker.exists()) {
            if (!checker.mkdir()) {
                System.err.println("access denied, try to start from another directory");
                System.exit(1);
            }
        }
        sessions = new Sessions(rootDirectory);
        resentErrors = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public Sessions getSessions() {
        return sessions;
    }

    public Set<Word> getResentErrors() {
        return resentErrors;
    }

    private String getSessionFileName(String session) {
        //если имя херовое - возвращает null
        return ((RootPaneController.FilePathTreeItem) treeView.getRoot()).findFileName(session);
    }

    public void saveSession(String session) {
        File file = new File(getSessionFileName(session));
        try {
            if (!file.exists() && sessions.get(session).size() > 0) {
                file.createNewFile();
            }

            try (PrintWriter out = new PrintWriter(file.getAbsolutePath(), "UTF-8")) {
                out.println(session);
                for (Word w : sessions.get(session)) {
                    out.println(w);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(5);
        }
    }
}
