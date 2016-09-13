package dron;

import com.sun.deploy.util.StringUtils;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class App extends Application {
    public static final String testDirectory = "C:\\projects\\debug";

    private Session currentSession;
    private Session resentErrors;
    private Sessions sessions;
    private Stage mainStage;
    private ComboBox<String> sessionsBox;
    private TreeView<String> treeView;
    private HBox root;

    public App() {
        sessions = new Sessions(testDirectory);
    }

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });

        HBox hBox = new HBox();
        hBox.getChildren().add(buildFileSystemBrowser());
        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        hBox.getChildren().addAll(sep, getSessionViewPane("s1"));
        root = hBox;

        Scene scene = new Scene(root, 500, 600);

        mainStage.setTitle("Active Vocabe");
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private VBox getSessionViewPane(String session) {
        TableView<Word> table = new TableView<>();

        final Label label = new Label("Words in session " + session + ":");
        label.setFont(new Font("Arial", 20));

        table.setEditable(true);

        TableColumn<Word, String> foreignCol = new TableColumn<>("Foreign");
        TableColumn<Word, String> translationsCol = new TableColumn<>("Translations");

        table.getColumns().addAll(foreignCol, translationsCol);

        foreignCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Word, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Word, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().getForeign());
            }
        });

        translationsCol.setCellValueFactory((p) -> {
            return new SimpleStringProperty(StringUtils.join(p.getValue().getTranslations(), ", "));
        });

        ObservableList<Word> items = FXCollections.observableList(sessions.get(session)
                .stream().collect(Collectors.toList()));
        table.setItems(items);

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, table);
        return vBox;
    }

    private TreeView buildFileSystemBrowser() {
        FilePathTreeItem root = new FilePathTreeItem(new File(testDirectory));
        treeView = new TreeView<>(root);
        treeView.setEditable(true);
        treeView.setCellFactory((TreeView<String> p) ->
                new TextFieldTreeCellImpl());
        return treeView;
    }

    private static class FilePathTreeItem extends TreeItem<String> {

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

        public TextFieldTreeCellImpl() {
            MenuItem addMenuItem = new MenuItem("Add Session");
            addMenuItem.setOnAction((ActionEvent t) -> {
                String newSession = "s" + Integer.toString((new Random()).nextInt());
                File file = new File(((FilePathTreeItem) getTreeItem()).getFullPath(), newSession);
                while (file.exists()){
                    newSession = "s" + Integer.toString((new Random()).nextInt());
                    file = new File(((FilePathTreeItem) getTreeItem()).getFullPath(), newSession);
                }
                sessions.add("new session");

//                try {
//                    if (!file.createNewFile()) {
//                        System.err.println("cannot create file!");
//                        System.exit(3);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    System.exit(3);
//                }
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
                root.getChildren().remove(2);
                root.getChildren().add(getSessionViewPane(((FilePathTreeItem) getTreeItem()).sessionName));
            });
            addWordsItem.setOnAction((e) -> {
                showAddWordPane(((FilePathTreeItem) getTreeItem()).sessionName);
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
                    (!sessions.contains(newValue) || item.isDirectory())) {

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
                    sessions.rename(getItem(), newValue);
                    item.sessionName = newValue;
                    if (sessions.get(newValue).size() > 0) {
                        saveSession(newValue);
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
                if (sessions.contains(newValue)) {
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

    private String getSessionFileName(String session) {
        //если имя херовое - возвращает null
        return ((FilePathTreeItem) treeView.getRoot()).findFileName(session);
    }

    private void saveSession(String session) {
        File file = new File(getSessionFileName(session));
        try {
            if (!file.exists() && sessions.get(session).size() > 0 ) {
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

    private void showAddWordPane(String session) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setOpaqueInsets(new Insets(10, 10, 10, 10));

        TextField foreign = new TextField();
        foreign.setPromptText("foreign");
        TextField translation = new TextField();
        translation.setPromptText("translations");
        foreign.setOnAction((e) -> translation.requestFocus());
        translation.setOnAction((e) -> {
            List<String> tr = new ArrayList<>();
            tr.add(translation.getText());
            sessions.get(session).add(new Word(foreign.getText(), tr));
            saveSession(session);
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

    private void showSelectQuizTypeDialog(){}

    private void showQuizDialog(){}

//    private Dialog<Boolean> getAddWordPane() {
//        Dialog<Boolean> dialog = new Dialog<>();
//        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.FINISH, ButtonType.CANCEL);
//
//        GridPane addWordForm = new GridPane();
//        addWordForm.setGridLinesVisible(true);
//
//        TextField foreign = new TextField();
//        TextField original = new TextField();
//
//        foreign.setPromptText("foreign");
//        original.setPromptText("translation");
//
//        foreign.setOnMouseClicked((ev) -> foreign.setText(""));
//        foreign.setOnAction((ev) -> {
//            original.setText("");
//            original.requestFocus();
//        });
//        original.setOnMouseClicked((ev) -> original.setText(""));
//        original.setOnAction((ev) -> {
//            currentSession.addWord(new Word(foreign.getText(), original.getText()));
//
//            foreign.setText("");
//            original.setText("");
//
//            currentSession.save();
//
//            foreign.requestFocus();
//        });
//
//
//        addWordForm.add(foreign, 0, 0);
//        addWordForm.add(original, 1, 0);
//
//// Почему-то это не прокатывает. При нажатии на кнопки обработчики не вызываются
////        Node applyBtn = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
////        Node finishBtn = dialog.getDialogPane().lookupButton(ButtonType.FINISH);
////
////        applyBtn.setOnMouseClicked(event -> {
////            System.out.println("apply");
////            currentSession.addWord(new Word(foreign.getText(), original.getText()));
////            foreign.setText("");
////            original.setText("");
////            currentSession.save();
////        });
////        finishBtn.setOnMouseClicked(event -> {
////            dialog.close();
////            System.out.println("finish");
////        });
//
//        dialog.getDialogPane().setContent(addWordForm);
//        dialog.setTitle("Add word");
//        dialog.setOnCloseRequest((e) -> {
//            if (!currentSession.getWords().isEmpty()) {
//                sessions.add(currentSession);
//                sessionsBox.getItems().add(currentSession.getName());
//                sessions.get(sessions.size() - 1).setId(sessions.size() - 1);
//                currentSession = null;
//                dialog.close();
//            }
//        });
//        return dialog;
//    }
//
//    private VBox getInitialPane() {
//        VBox mainBox = new VBox(20);
//        HBox hBox = new HBox(10);
//        sessionsBox = new ComboBox<>();
//        sessionsBox.setId("sessionComboBox");
//        ObservableList<String> names = FXCollections.observableArrayList();
//        names.add("new session");
//
//        for (Session s : sessions) {
//            names.add(s.getName());
//        }
//
//        sessionsBox.setItems(names);
//        hBox.getChildren().add(sessionsBox);
//        mainBox.getChildren().add(hBox);
//
//        sessionsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                TextField sessionName = new TextField();
//                sessionName.setPromptText("session name");
//
//                if (sessionsBox.getValue().equals("new session")) {
//                    hBox.getChildren().add(sessionName);
//                } else {
//                    if (hBox.getChildren().size() > 1)
//                        hBox.getChildren().remove(1);
//                    if (mainBox.getChildren().size() > 1)
//                        mainBox.getChildren().remove(1);
//
//                    Session displayed = sessions.stream()
//                            .filter(s -> s.getName().equals(sessionsBox.getValue()))
//                            .collect(Collectors.toList()).get(0);
//
//                    ScrollPane scrollPane = new ScrollPane(getSessionWordsPane(displayed));
//                    mainBox.getChildren().add(scrollPane);
//                }
//
//                sessionName.setOnAction((event) -> {
//                    currentSession = new Session(sessionName.getText());
//                    getAddWordPane().showAndWait();
//                });
//            }
//        });
//
//        Button testBtn = new Button("Test");
//        testBtn.setOnAction((e) -> {
//            Optional<Pair<String, String>> result = getQuizSelectionDialog().showAndWait();
//            if(!result.get().getKey().equals("Session")){
//                getQuizStage(5, result.get().getKey()).showAndWait();
//            }
//            else{
//                getQuizStage(5, result.get().getValue()).showAndWait();
//            }
//        });
//        mainBox.getChildren().add(testBtn);
//
//        return mainBox;
//    }
//
//    private GridPane getSessionWordsPane(Session session) {
//        GridPane gridPane = new GridPane();
//        int rowIndex = 0;
//        for (Word word : session.getWords()) {
//            Text foreign = new Text(word.getForeign());
//            Text original = new Text(word.getOriginal());
//            gridPane.addRow(rowIndex, foreign, original);
//            rowIndex++;
//        }
//        gridPane.setHgap(20);
//        gridPane.setVgap(10);
//        gridPane.setPadding(new Insets(10, 10, 10, 10));
//
//        return gridPane;
//    }
//
//    private Dialog<Pair<String, String>> getQuizSelectionDialog() {
//        Dialog<Pair<String, String>> dialog = new Dialog<>();
//        dialog.setTitle("Choose test format");
//
//        ButtonType btnOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
//        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);
//
//        Node okButton = dialog.getDialogPane().lookupButton(btnOk);
//        okButton.setDisable(true);
//
//        final ToggleGroup group = new ToggleGroup();
//
//        RadioButton rbRandom = new RadioButton("Random");
//        rbRandom.setToggleGroup(group);
//        rbRandom.setUserData("Random");
//
//        RadioButton rbWorst = new RadioButton("Worst");
//        rbWorst.setToggleGroup(group);
//        rbWorst.setUserData("Worst");
//
//        RadioButton rbSession = new RadioButton("Session");
//        rbSession.setToggleGroup(group);
//        rbSession.setUserData("Session");
//
//        ComboBox<String> sessionChoiceBox = new ComboBox<>();
//        sessionChoiceBox.setOnAction((ev) -> {
//            if (sessionChoiceBox.getValue() == null) {
//                okButton.setDisable(true);
//            } else {
//                okButton.setDisable(false);
//            }
//        });
//
//        ObservableList<String> names = FXCollections.observableArrayList();
//        names.addAll(sessions.stream().map(Session::getName).collect(Collectors.toList()));
//
//        group.selectedToggleProperty().addListener(
//                (ObservableValue<? extends Toggle> ov, Toggle old_toggle,
//                 Toggle new_toggle) -> {
//                    if (group.getSelectedToggle().getUserData().toString().equals("Session")) {
//                        sessionChoiceBox.setItems(names);
//                        sessionChoiceBox.setVisible(true);
//                        okButton.setDisable(true);
//                    } else {
//                        sessionChoiceBox.setVisible(false);
//                        sessionChoiceBox.setValue(null);
//                        okButton.setDisable(false);
//                    }
//                });
//
//        GridPane grid = new GridPane();
//        grid.add(rbRandom, 0, 0);
//        grid.add(rbWorst, 0, 1);
//        grid.add(rbSession, 0, 2);
//        grid.add(sessionChoiceBox, 1, 2);
//
//        dialog.getDialogPane().setContent(grid);
//        dialog.setResultConverter(dialogButton -> {
////            Stage quizStage;
////            int sizeOfQuiz = 10;
//            if (dialogButton == btnOk) {
////                switch (group.getSelectedToggle().getUserData().toString()) {
////                    case "Random":
////                        quizStage = getQuizStage(sizeOfQuiz, "Random");
////                        break;
////                    case "Worst":
////                        quizStage = getQuizStage(sizeOfQuiz, "Worst");
////                        break;
////                    default:
////                        quizStage = getQuizStage(sizeOfQuiz, sessionChoiceBox.getValue());
////                }
//                return new Pair<>(group.getSelectedToggle()
//                        .getUserData().toString(), sessionChoiceBox.getValue());
//            }
//            return null;
//        });
//
//        return dialog;
//    }
//
//    private Stage getQuizStage(int size_, String quizType) {
//        Random random = new Random();
//        List<Word> allWords;
//        if (quizType.equals("Random") || quizType.equals("Worst")) {
//            allWords = sessions.stream().map(Session::getWords).flatMap(l -> l.stream())
//                    .collect(Collectors.toList());
//        } else {
//            allWords = sessions.stream().filter(s -> s.getName().equals(quizType))
//                    .collect(Collectors.toList()).get(0).getWords().stream().collect(Collectors.toList());
//
//// ЧЕРТОВЩИНА КАКАЯ-ТО. КОД НИЖЕ НЕ РАБОТАЕТ
////            allWords = sessions.stream().filter(s -> s.getName().equals(quizType))
////                    .collect(Collectors.toList()).get(0).getWords();
//        }
//
//        if (quizType.equals("Random")) {
//            allWords.sort((a, b) -> random.nextInt(20) - 10);
//        }
//        if (quizType.equals("Worst")) {
//            allWords.sort((a, b) -> a.getKnowledge() - b.getKnowledge());
//        }
//        int size = min(size_, allWords.size());
//        List<Word> testWords = allWords.subList(0, size);
//
//        List<Pair<Integer, Integer>> wordsIds = new ArrayList<>(size);
//        testWords.forEach(word -> {
//            int sessionId = word.getSessionId();
//            int wordId = sessions.get(sessionId).getWords().indexOf(word);
//            wordsIds.add(new Pair<>(sessionId, wordId));
//        });
//
//        Stage quizStage = new Stage();
//        currentIndex = 0;
//        VBox vBox = new VBox();
//        Text question = new Text();
//        question.setText(testWords.get(0).getForeign());
//        TextField answer = new TextField();
//        answer.setPromptText("your answer");
//        answer.setOnAction((event) -> {
//            boolean correct = answer.getText().equals(testWords.get(currentIndex).getOriginal());
//
//            Session session = sessions.get(wordsIds.get(currentIndex).getKey());
//            session.updateWordKnowledge(wordsIds.get(currentIndex).getValue(), correct);
//            sessions.remove((int) wordsIds.get(currentIndex).getKey());
//            sessions.add(wordsIds.get(currentIndex).getKey(), session);
//            session.save();
//
//            if (!correct) {
//                resentErrors.addWord(testWords.get(currentIndex));
//            }
//            currentIndex++;
//            if (currentIndex == size) quizStage.close();
//            else {
//                question.setText(testWords.get(currentIndex).getForeign());
//                answer.setText("");
//            }
//        });
//        vBox.getChildren().addAll(question, answer);
//        Scene scene = new Scene(vBox);
//        quizStage.setScene(scene);
//        return quizStage;
//    }
//
//    private int currentIndex;
}
