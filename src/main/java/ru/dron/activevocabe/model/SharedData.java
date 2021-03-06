package ru.dron.activevocabe.model;

import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import ru.dron.activevocabe.controllers.RootPaneController;

import java.io.File;
import java.io.PrintWriter;

/**
 * Created by Andrey on 15.10.2016.
 */
public class SharedData {
    public static final String CSS_PATH = "/css/styles.css";
    private final String rootDirectory;
    private Sessions sessions;
    private Stage rootStage;
    private String currentSession;
    private TreeView<String> treeView;

    private QuizProperties lastQuizProperties;
    private QuizResult lastQuizResult;
    private boolean repassRequired = false;

    private static SharedData sharedData;

    public static SharedData getSharedData() {
        if (sharedData == null) {
            sharedData = new SharedData();
        }
        return sharedData;
    }

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

    public void setLastQuizProperties(QuizProperties newProperties) {
        lastQuizProperties = newProperties;
    }

    public QuizProperties getLastQuizProperties() {
        return lastQuizProperties;
    }

    public void setLastQuizResult(QuizResult result) {
        lastQuizResult = result;
    }

    public QuizResult getLastQuizResult() {
        return lastQuizResult;
    }

    public void setRepassRequired(boolean required) {
        repassRequired = required;
    }

    public boolean isRepassRequired() {
        return repassRequired;
    }

    public String getRootDirectory() {
        return rootDirectory;
    }

    public Stage getRootStage() {
        return rootStage;
    }

    public void setCurrentSession(String session) {
        currentSession = session;
    }

    public String getCurrentSession() {
        return currentSession;
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
