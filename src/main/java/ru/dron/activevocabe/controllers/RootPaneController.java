package ru.dron.activevocabe.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import ru.dron.activevocabe.FileTransformer;
import ru.dron.activevocabe.QuizManager;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.TWord;
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
    private AnchorPane root;
    @FXML
    private Label label;
    @FXML
    private TableView<TWord> tableView;
    @FXML
    private TableColumn<TWord, String> foreignCol;
    @FXML
    private TableColumn<TWord, String> translationsCol;
    @FXML
    private TreeView<String> treeView;
    @FXML
    private TextField newForeign;
    @FXML
    private TextField newTranslation;

    private ContextMenu sessionMenu;
    private ContextMenu folderMenu;

    @FXML
    public void initialize() {
        //treeView initialization
        sharedData.setTreeView(treeView);

        FilePathTreeItem root = new FilePathTreeItem(new File(sharedData.getRootDirectory()));
        root.setExpanded(true);
        treeView.setRoot(root);
        treeView.setEditable(true);
        treeView.setCellFactory((TreeView<String> p) ->
                new TextFieldTreeCellImpl());
        treeView.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.isLeaf()) {
                updateWordsTable(newValue.getValue());
            }
        }));
        initializeTreeViewMenus();

        //tableView initialization
        foreignCol.setCellValueFactory(new PropertyValueFactory<>("foreign"));
        foreignCol.setCellFactory(TextFieldTableCell.forTableColumn());
        translationsCol.setCellValueFactory(new PropertyValueFactory<>("translation"));
        translationsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        foreignCol.setOnEditCommit((t) -> {
            TWord curr = t.getTableView().getItems().get(t.getTablePosition().getRow());
            Word prev = curr.getWord();
            curr.setForeign(t.getNewValue());
            Word newword = curr.getWord();
            sharedData.getSessions().replaceWord(sharedData.getCurrentSession(), prev, newword);
            sharedData.saveSession(sharedData.getCurrentSession());
        });
        translationsCol.setOnEditCommit((t) -> {
            TWord curr = t.getTableView().getItems().get(t.getTablePosition().getRow());
            Word prev = curr.getWord();
            curr.setTranslation(t.getNewValue());
            Word newword = curr.getWord();
            sharedData.getSessions().replaceWord(sharedData.getCurrentSession(), prev, newword);
            sharedData.saveSession(sharedData.getCurrentSession());
        });

        MenuItem removeLine = new MenuItem("remove this word");
        removeLine.setOnAction(event -> {
            sharedData.getSessions().removeWord(sharedData.getCurrentSession(),
                    tableView.getSelectionModel().getSelectedItem().getWord());
            tableView.getItems().remove(tableView.getSelectionModel().getSelectedItem());
        });
        removeLine.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN));
        tableView.setContextMenu(new ContextMenu(removeLine));
    }

    private void updateWordsTable(String session) {
        sharedData.setCurrentSession(session);
        ObservableList<TWord> items = FXCollections.observableList(sharedData.getSessions().get(session)
                .stream().map(TWord::new).collect(Collectors.toList()));
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

    private class TextFieldTreeCellImpl extends TreeCell<String> {
        private TextField textField;

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
                    if (((FilePathTreeItem)getTreeItem()).isDirectory()) {
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

    @FXML
    private void onLoadPressed() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FileReader.fxml"));
            loader.load();

            ((DialogController) loader.getController()).getDialogStage().showAndWait();

            ObservableList<TWord> ol = FXCollections.observableArrayList(
                    FileTransformer.getInstance().readFile()
                            .stream().map(w -> new TWord(w)).collect(Collectors.toList()));

            loader = new FXMLLoader(getClass().getResource("/fxml/LoaddedWordsCheck.fxml"));

            try {
                loader.load();
                ((WordsCheckController) loader.getController()).setWords(ol);
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1500);
            }
            ((DialogController) loader.getController()).getDialogStage().showAndWait();

            updateWordsTable(sharedData.getCurrentSession());
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    @FXML
    private void onQuiz() {
        QuizManager.getInstance().run();
    }

    @FXML
    private void onAddWord() {
        if (!newForeign.getText().matches("\\s*") && !newTranslation.getText().matches("\\s*")) {
            Word w = new Word(newForeign.getText(), newTranslation.getText());
            sharedData.getSessions().add(sharedData.getCurrentSession(), w);
            sharedData.saveSession(sharedData.getCurrentSession());
            tableView.getItems().add(new TWord(w));

            newForeign.clear();
            newTranslation.clear();
            newForeign.requestFocus();
        }
    }

    @FXML
    private void onNewForeignAction() {
        newTranslation.clear();
        newTranslation.requestFocus();
    }

    private void initializeTreeViewMenus(){

        MenuItem startQuiz = new MenuItem("Quiz");
        startQuiz.setOnAction((e) -> {
            QuizManager.getInstance().run(treeView.getSelectionModel().getSelectedItem().getValue());
        });
        startQuiz.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        sessionMenu = new ContextMenu(startQuiz);

        MenuItem addMenuItem = new MenuItem("Add Session");
        addMenuItem.setOnAction((ActionEvent t) -> {
            FilePathTreeItem item = (FilePathTreeItem) treeView.getSelectionModel().getSelectedItem();
            String newSession = "s" + Integer.toString((new Random()).nextInt());
            File file = new File(item.getFullPath(), newSession);
            while (file.exists()) {
                newSession = "s" + Integer.toString((new Random()).nextInt());
                file = new File(item.getFullPath(), newSession);
            }
            sharedData.getSessions().add("new session");

            item.getChildren().add(new FilePathTreeItem(file, "new session"));
        });
        MenuItem addDirItem = new MenuItem("Add Folder");
        addDirItem.setOnAction((ActionEvent t) -> {
            FilePathTreeItem item = (FilePathTreeItem) treeView.getSelectionModel().getSelectedItem();
            File f = new File(item.getFullPath(), "new folder");
            try {
                Path toDir = Files.createDirectory(Paths.get(f.getPath()));
                item.getChildren().add(new FilePathTreeItem(toDir.toFile()));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(3);
            }
        });
        folderMenu = new ContextMenu(addDirItem, addMenuItem);
    }
}
