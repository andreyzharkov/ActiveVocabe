package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.QuizManager;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 15.10.2016.
 */
public class RootPaneController {
    private SharedData sharedData = SharedData.getSharedData();

    @FXML
    private HBox root;
    @FXML
    private Label label;
    @FXML
    private TableView<Word> tableView;
    @FXML
    private TableColumn<Word, String> foreignCol;
    @FXML
    private TableColumn<Word, String> translationsCol;
    @FXML
    private TreeView<String> treeView;

    @FXML
    public void initialize() {
        sharedData.setTreeView(treeView);

        FilePathTreeItem root = new FilePathTreeItem(new File(sharedData.getRootDirectory()));
        root.setExpanded(true);
        treeView.setRoot(root);
        treeView.setEditable(true);
        treeView.setCellFactory((TreeView<String> p) ->
                new TextFieldTreeCellImpl());


        foreignCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getForeign()));
        translationsCol.setCellValueFactory(p ->
                new SimpleStringProperty(StringUtils
                        .join(p.getValue().getTranslations(), ", ")));
    }

    private void updateWordsTable(String session) {
        ObservableList<Word> items = FXCollections.observableList(sharedData.getSessions().get(session)
                .stream().collect(Collectors.toList()));
        tableView.setItems(items);

        label.setText("Words in session " + session + ":");
    }

    public static class FilePathTreeItem extends TreeItem<String> {

        private boolean isDirectory;
        private String fullPath;
        //for files only
        private String sessionName;

        public boolean isDirectory() {
            return (this.isDirectory);
        }

        public String getFullPath() {
            return fullPath;
        }

        public FilePathTreeItem(File file) {
            super(file.getName());
            this.fullPath = file.toString();
            if (file.isDirectory()) {
                this.isDirectory = true;
                super.getChildren().setAll(buildChildren(fullPath));
            } else {
                this.isDirectory = false;

                //выясняем имя сессии
                try {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(
                            new FileInputStream(file.getAbsoluteFile()), "UTF8"))) {
                        if ((sessionName = in.readLine()) == null) {
                            throw new Exception("Can't read session name!");
                        }
                        setValue(sessionName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(4);
                }
            }
        }

        public FilePathTreeItem(File file, String sessionName) {
            super(file.getName());
            this.fullPath = file.toString();
            if (file.isDirectory()) {
                this.isDirectory = true;
                super.getChildren().setAll(buildChildren(fullPath));
            } else {
                this.isDirectory = false;

                //выясняем имя сессии
                this.sessionName = sessionName;
                setValue(sessionName);
            }
        }

        @Override
        public boolean isLeaf() {
            return !isDirectory;
        }

        //по имени сессии ищем имя файла (вглубину), где она записана
        public String findFileName(String sessionNameQuery) {
            if (isDirectory()) {
                for (TreeItem<String> child : getChildren()) {
                    String res = ((FilePathTreeItem) child).findFileName(sessionNameQuery);
                    if (res != null) {
                        return res;
                    }
                }
                return null;
            } else {
                if (sessionNameQuery.equals(sessionName)) {
                    return fullPath;
                }
                return null;
            }
        }

        private static ObservableList<FilePathTreeItem> buildChildren(String path) {
            File f = new File(path);
            if (f != null && f.isDirectory()) {
                File[] files = f.listFiles();
                if (files != null) {
                    ObservableList<FilePathTreeItem> children = FXCollections.observableArrayList();

                    for (File childFile : files) {
                        children.add(new FilePathTreeItem(childFile));
                    }

                    return children;
                }
            }

            return FXCollections.emptyObservableList();
        }
    }

    private final class TextFieldTreeCellImpl extends TreeCell<String> {

        private TextField textField;
        private final ContextMenu folderMenu = new ContextMenu();
        private final ContextMenu sessionMenu = new ContextMenu();

        TextFieldTreeCellImpl() {
            MenuItem addMenuItem = new MenuItem("Add Session");
            addMenuItem.setOnAction((ActionEvent t) -> {
                String newSession = "s" + Integer.toString((new Random()).nextInt());
                File file = new File(((FilePathTreeItem) getTreeItem()).getFullPath(), newSession);
                while (file.exists()) {
                    newSession = "s" + Integer.toString((new Random()).nextInt());
                    file = new File(((FilePathTreeItem) getTreeItem()).getFullPath(), newSession);
                }
                sharedData.getSessions().add("new session");

                getTreeItem().getChildren().add(new FilePathTreeItem(file, "new session"));
            });

            MenuItem addDirItem = new MenuItem("Add Folder");
            addDirItem.setOnAction((ActionEvent t) -> {
                File f = new File(((FilePathTreeItem) getTreeItem()).getFullPath(), "new folder");
                try {
                    Path toDir = Files.createDirectory(Paths.get(f.getPath()));
                    getTreeItem().getChildren().add(new FilePathTreeItem(toDir.toFile()));
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(3);
                }
            });

            folderMenu.getItems().addAll(addDirItem, addMenuItem);

            MenuItem addWordsItem = new MenuItem("Add word");
            MenuItem viewWordsItem = new MenuItem("View words");
            MenuItem startQuiz = new MenuItem("quiz");

            viewWordsItem.setOnAction((e) -> {
                updateWordsTable(((FilePathTreeItem) getTreeItem()).sessionName);
            });
            addWordsItem.setOnAction((e) -> {
                showAddWordPane(((FilePathTreeItem) getTreeItem()).sessionName);
            });
            startQuiz.setOnAction((e) -> {
                QuizManager.getInstance().run();
            });

            sessionMenu.getItems().addAll(addWordsItem, viewWordsItem, startQuiz);
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }

        @Override
        public void commitEdit(String newValue) {
            textField.setText("");
            //dirty hack
            FilePathTreeItem item = (FilePathTreeItem) getTreeItem();

            if (!newValue.equals("") &&
                    (!sharedData.getSessions().contains(newValue) || item.isDirectory())) {

                File file = new File(item.getFullPath());
                if (item.isDirectory()) {
                    if (!file.renameTo(new File(file.getParent() + File.separator + newValue))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error!");
                        alert.setHeaderText("Because of some system restrictions file wasn't renamed!");
                        alert.setContentText("Try to rerun program.");
                        alert.showAndWait();

                        super.commitEdit(getItem());
                    } else {
                        super.commitEdit(newValue);
                    }
                } else {
                    sharedData.getSessions().rename(getItem(), newValue);
                    item.sessionName = newValue;
                    if (sharedData.getSessions().get(newValue).size() > 0) {
                        sharedData.saveSession(newValue);
                    }

                    super.commitEdit(newValue);
                }

            } else {
                super.commitEdit(getItem());
                if (newValue.equals("")) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setContentText("Enter session name, session name is not changed.");
                    alert.showAndWait();
                }
                if (sharedData.getSessions().contains(newValue)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setContentText("Session with the same name already exists, try to use another name or" +
                            " rename that session.");
                    alert.showAndWait();
                }
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(getItem());
            setGraphic(getTreeItem().getGraphic());
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    //setText(null);
                    setGraphic(textField);
                } else {
                    setText(getTreeItem().getValue());
                    setGraphic(getTreeItem().getGraphic());
                    if (!getTreeItem().isLeaf()) {
                        setContextMenu(folderMenu);
                    } else {
                        setContextMenu(sessionMenu);
                    }
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setOnKeyReleased((KeyEvent t) -> {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });

        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }

    private void showAddWordPane(String session) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setOpaqueInsets(new Insets(10, 10, 10, 10));

        TextField foreign = new TextField();
        foreign.setPromptText("foreign");
        TextField translation = new TextField();
        translation.setPromptText("tr-s, sep is \';\'");
        foreign.setOnAction((e) -> translation.requestFocus());
        translation.setOnAction((e) -> {
            List<String> tr = new ArrayList<>();
            Arrays.asList(translation.getText().split(";")).forEach(t -> {
                if (!t.equals("") && !t.matches("\\s")) {
                    tr.add(StringUtils.join(Arrays.stream(t.split("\\s"))
                            .filter(s -> !s.equals(""))
                            .collect(Collectors.toList()), " "));
                }
            });
            System.out.println(tr);
            sharedData.getSessions().get(session).add(new Word(foreign.getText(), tr));
            sharedData.saveSession(session);
            foreign.clear();
            translation.clear();
            foreign.requestFocus();
        });

        grid.addRow(0, foreign, translation);
        Dialog<Boolean> dialog = new Dialog<>();
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(grid);
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("Add new word to session " + session);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }
}
