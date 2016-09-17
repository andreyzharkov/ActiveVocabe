package ru.dron.activevocabe;

import org.apache.commons.lang3.StringUtils;
import com.google.common.base.CaseFormat;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class App extends Application {
    private final String rootDirectory;

    private Sessions sessions;
    private Set<Word> resentErrors;
    private TreeView<String> treeView;
    private HBox root;

    public App() {
        rootDirectory = System.getProperty("user.dir") + File.separator + "root";
        File checker = new File(rootDirectory);
        if (!checker.exists()){
            if (!checker.mkdir()){
                System.err.println("access denied, try to start from another directory");
                System.exit(1);
            }
        }
        sessions = new Sessions(rootDirectory);
        resentErrors = Collections.synchronizedSet(new LinkedHashSet<>());
    }

    @Override
    public void start(Stage primaryStage) {
        Stage mainStage = primaryStage;

        HBox hBox = new HBox();
        hBox.getChildren().add(buildFileSystemBrowser());
        Separator sep = new Separator();
        sep.setOrientation(Orientation.VERTICAL);
        hBox.getChildren().addAll(sep);
        root = hBox;

        Scene scene = new Scene(root, 800, 600);
        String cssPath = "/css/styles.css";
        scene.getStylesheets().add(cssPath);

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

        TableColumn<Word, String> foreignCol = new TableColumn<>("Foreign");
        TableColumn<Word, String> translationsCol = new TableColumn<>("Translations");

        table.getColumns().setAll(foreignCol, translationsCol);
        foreignCol.setMinWidth(200);
        translationsCol.setMinWidth(400);
        foreignCol.setMaxWidth(Double.MAX_VALUE);
        translationsCol.setMaxWidth(Double.MAX_VALUE);

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
        table.autosize();
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);

        VBox vBox = new VBox(10, label, table);
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.setMaxHeight(Double.MAX_VALUE);
        return vBox;
    }

    private VBox getErrorsViewPane() {
        if (resentErrors.size() == 0) {
            Text congratulation = new Text("Congratulations! You made no mistakes in this test!");
            VBox vBox = new VBox();
            vBox.getChildren().add(congratulation);
            return vBox;
        }

        TableView<Word> table = new TableView<>();

        final Label label = new Label("Your errors:");
        label.setFont(new Font("Arial", 20));

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

        ObservableList<Word> items = FXCollections.observableList(resentErrors
                .stream().collect(Collectors.toList()));
        table.setItems(items);

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, table);
        return vBox;
    }

    private TreeView buildFileSystemBrowser() {
        FilePathTreeItem root = new FilePathTreeItem(new File(rootDirectory));
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
                while (file.exists()) {
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
                if (root.getChildren().size() > 2) {
                    root.getChildren().remove(2);
                }
                root.getChildren().add(getSessionViewPane(((FilePathTreeItem) getTreeItem()).sessionName));
            });
            addWordsItem.setOnAction((e) -> {
                showAddWordPane(((FilePathTreeItem) getTreeItem()).sessionName);
            });
            startQuiz.setOnAction((e) -> {
                showSelectQuizTypeDialog();
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
            Arrays.asList(translation.getText().split(";")).forEach(t ->{
                if (!t.equals("") && !t.matches("\\s")){
                    tr.add(StringUtils.join(Arrays.stream(t.split("\\s"))
                            .filter(s -> !s.equals(""))
                            .collect(Collectors.toList()), " "));
                }
            });
            System.out.println(tr);
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

    private void showSelectQuizTypeDialog() {
        Dialog<QuizProperties> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setOpaqueInsets(new Insets(10, 10, 10, 10));

        ToggleGroup radioGroup = new ToggleGroup();
        radioGroup.setUserData(null);
        RadioButton randomBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.RANDOM.toString().toLowerCase()));
        RadioButton ratingBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.RATING.toString().toLowerCase()));
        RadioButton sessionBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.SESSION.toString().toLowerCase()));
        RadioButton errorsBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.ERRORS.toString().toLowerCase()));
        ComboBox<String> sessionChoiceBox = new ComboBox<>(FXCollections
                .observableList(sessions.getKeys()
                        .stream().collect(Collectors.toList())));
        sessionChoiceBox.setOnAction((e) -> {
            if (sessionChoiceBox.getValue() != null) {
                radioGroup.setUserData(sessionBtn.getText());
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            }
        });
        sessionChoiceBox.setValue(null);
        sessionChoiceBox.setDisable(true);

        randomBtn.setToggleGroup(radioGroup);
        ratingBtn.setToggleGroup(radioGroup);
        sessionBtn.setToggleGroup(radioGroup);
        errorsBtn.setToggleGroup(radioGroup);

        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        randomBtn.setOnAction((e) -> {
            radioGroup.setUserData(randomBtn.getText());
            sessionChoiceBox.setValue(null);
            sessionChoiceBox.setDisable(true);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });
        ratingBtn.setOnAction((e) -> {
            radioGroup.setUserData(ratingBtn.getText());
            sessionChoiceBox.setValue(null);
            sessionChoiceBox.setDisable(true);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });
        sessionBtn.setOnAction((e) -> {
            radioGroup.setUserData("");
            sessionChoiceBox.setDisable(false);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        });
        errorsBtn.setOnAction((e) -> {
            radioGroup.setUserData(ratingBtn.getText());
            sessionChoiceBox.setValue(null);
            sessionChoiceBox.setDisable(true);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });

        Label testSizeLabel = new Label("Size of test: ");
        TextField testSizeInput = new TextField("0");
        testSizeInput.setOnAction((e) -> {
            if (dialog.getDialogPane().lookupButton(ButtonType.OK).isDisable()
                    && sessionChoiceBox.getValue() != null) {
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            }
            try {
                Integer.parseInt(testSizeInput.getText());
            } catch (NumberFormatException ex) {
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            }
        });

        VBox radios = new VBox(randomBtn, ratingBtn, sessionBtn);
        if (!resentErrors.isEmpty()) {
            radios.getChildren().add(errorsBtn);
        }

        RadioButton foreignBtn = new RadioButton("Foreign input");
        RadioButton translationBtn = new RadioButton("Translation input");
        ToggleGroup languageGroup = new ToggleGroup();
        foreignBtn.setToggleGroup(languageGroup);
        translationBtn.setToggleGroup(languageGroup);
        foreignBtn.setSelected(true);
        foreignBtn.setUserData(true);
        translationBtn.setUserData(false);

        gridPane.add(radios, 0, 0, 1, 4);
        gridPane.add(sessionChoiceBox, 1, 4);
        gridPane.add(testSizeLabel, 0, 5);
        gridPane.add(testSizeInput, 1, 5, 2, 1);
        gridPane.add(foreignBtn, 0, 6, 2, 1);
        gridPane.add(translationBtn, 0, 7, 2, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter((btn) -> {
            if (btn.equals(ButtonType.CANCEL)) return null;
            String resultType = radioGroup.getUserData().toString();
            int testSize = Integer.parseInt(testSizeInput.getText());

            if (resultType.equals(randomBtn.getText())) {
                return new QuizProperties(QuizType.RANDOM, testSize,
                        (boolean) languageGroup.getSelectedToggle().getUserData());
            }
            if (resultType.equals(ratingBtn.getText())) {
                return new QuizProperties(QuizType.RATING, testSize,
                        (boolean) languageGroup.getSelectedToggle().getUserData());
            }
            if (resultType.equals(errorsBtn.getText())) {
                return new QuizProperties(QuizType.ERRORS, testSize,
                        (boolean) languageGroup.getSelectedToggle().getUserData());
            }

            return new QuizProperties(QuizType.SESSION, testSize,
                    (boolean) languageGroup.getSelectedToggle().getUserData(),
                    sessionChoiceBox.getValue());
        });

        Optional<QuizProperties> result = dialog.showAndWait();
        if (result.isPresent()) {
            showQuizDialog(result.get());
        }
    }

    private void showQuizDialog(QuizProperties properties) {
        Dialog<Integer> dialog = new Dialog<>();

        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setOpaqueInsets(new Insets(5, 5, 5, 5));

        Text question = new Text();
        TextField answer = new TextField();
        answer.setPromptText("Your answer");

        List<Word> questionList = new ArrayList<>();

        if (properties.getQuizType().equals(QuizType.RANDOM)) {
            Random random = new Random();
            questionList = sessions.getValues().stream().flatMap(Set::stream)
                    .sorted((o1, o2) -> random.nextInt(11) - 5)
                    .limit(properties.getNumberOfQuestions())
                    .collect(Collectors.toList());
        }
        if (properties.getQuizType().equals(QuizType.RATING)) {
            questionList = sessions.getValues().stream().flatMap(Set::stream)
                    .sorted(Word.getKnowledgeComparator())
                    .limit(properties.getNumberOfQuestions())
                    .collect(Collectors.toList());
        }
        if (properties.getQuizType().equals(QuizType.SESSION)) {
            questionList = sessions.get(properties.getSessionName())
                    .stream()
                    .sorted(Word.getKnowledgeComparator())
                    .limit(properties.getNumberOfQuestions())
                    .collect(Collectors.toList());
        }
        if (properties.getQuizType().equals(QuizType.ERRORS)) {
            questionList = resentErrors.stream().collect(Collectors.toList());
        }

        final List<Word> testList = questionList;

        correctAnswers = 0;
        currentIndex = 0;
        resentErrors = Collections.synchronizedSet(new HashSet<>());

        if (properties.isQuestionsOnForeign()) {
            question.setText(testList.get(currentIndex).getForeign());
        } else {
            question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
        }

        answer.setOnAction((e) -> {
            boolean correct;
            if (properties.isQuestionsOnForeign()){
                correct = testList.get(currentIndex).getTranslations().contains(answer.getText());
            } else{
                correct = testList.get(currentIndex).getForeign().equals(answer.getText());
            }

            if (correct) {
                correctAnswers++;
            } else {
                resentErrors.add(testList.get(currentIndex));
            }

            sessions.getWord(testList.get(currentIndex)).updateKnowledge(correct);
            saveSession(sessions.getKeyOf(testList.get(currentIndex)));

            currentIndex++;
            if (currentIndex == testList.size()) {
                dialog.setResult(correctAnswers);
                dialog.close();
                showQuizResult(testList.size());
            } else {
                if (properties.isQuestionsOnForeign()) {
                    question.setText(testList.get(currentIndex).getForeign());
                } else {
                    question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
                }
                answer.setText("");
            }
        });

        vBox.getChildren().addAll(question, answer);
        dialog.getDialogPane().setContent(vBox);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        dialog.showAndWait();
    }

    private void showQuizResult(int wordsInQuiz) {
        Text persentage = new Text((new DecimalFormat("#.00")).format(
                ((double) correctAnswers) / wordsInQuiz * 100) + "%");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setOpaqueInsets(new Insets(10, 10, 10, 10));

        grid.add(persentage, 0, 0);
        grid.add(getErrorsViewPane(), 0, 1, 10, 10);

        Dialog dialog = new Dialog();
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
        dialog.showAndWait();
    }

    private enum QuizType {
        RANDOM,
        RATING,
        SESSION,
        ERRORS
    }

    private static class QuizProperties {
        private String sessionName;
        private int numberOfQuestions;
        private QuizType type;
        private boolean isQuestionsOnForeign;

        QuizProperties(QuizType type, int numberOfQuestions, boolean isQuestionsOnForeign) {
            this.type = type;
            this.numberOfQuestions = numberOfQuestions;
            this.isQuestionsOnForeign = isQuestionsOnForeign;
        }

        QuizProperties(QuizType type, int numberOfQuestions, boolean isQuestionsOnForeign,
                       String sessionName) {
            this.type = type;
            this.numberOfQuestions = numberOfQuestions;
            this.isQuestionsOnForeign = isQuestionsOnForeign;
            this.sessionName = sessionName;
        }

        public QuizType getQuizType() {
            return type;
        }

        public int getNumberOfQuestions() {
            return numberOfQuestions;
        }

        public String getSessionName() {
            return sessionName;
        }

        public boolean isQuestionsOnForeign() {
            return isQuestionsOnForeign;
        }
    }

    //костыли
    private int correctAnswers;
    private int currentIndex;
}
