package ru.dron.activevocabe.model;

import javafx.scene.control.TreeView;
import javafx.stage.Stage;
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
    private Stage rootStage;
    private TreeView<String> treeView;

    private static SharedData sharedData;

    public SharedData() {
        rootDirectory = System.getProperty("user.dir") + File.separator + "root";
        File checker = new File(rootDirectory);
        if (!checker.exists()) {
            if (!checker.mkdir()) {
                System.err.println("access denied, try to start from another directory");
                System.exit(1);
            }
        }
        sessions = new Sessions(rootDirectory);
    }

    //it should be called only once. need to set protection
    public void setTreeView(TreeView<String> treeView) {
        this.treeView = treeView;
    }

    public void setRootStage(Stage rootStage) {
        this.rootStage = rootStage;
    }

    public static SharedData getSharedData() {
        if (sharedData == null) {
            sharedData = new SharedData();
        }
        return sharedData;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public Stage getRootStage(){
        return rootStage;
    }

    public Sessions getSessions() {
        return sessions;
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
