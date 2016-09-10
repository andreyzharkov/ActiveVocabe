package dron;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class App extends Application {
    public static final String testDirectory = "C:\\projects\\debug";

    private Session currentSession;
    private Session resentErrors;
    private List<Session> sessions;
    private Stage mainStage;
    private ComboBox<String> sessionsBox;

    public App() {
        sessions = new ArrayList<>();
        resentErrors = new Session("errors");
        try {
            List<File> files = Files.walk(Paths.get(testDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : files) {
                sessions.add(new Session(file, sessions.size()));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
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

        Scene scene = new Scene(getInitialPane(), 500, 600);

        mainStage.setTitle("Active Vocabe");
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private Dialog<Boolean> getAddWordPane() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.FINISH, ButtonType.CANCEL);

        GridPane addWordForm = new GridPane();
        addWordForm.setGridLinesVisible(true);

        TextField foreign = new TextField();
        TextField original = new TextField();

        foreign.setPromptText("foreign");
        original.setPromptText("translation");

        foreign.setOnMouseClicked((ev) -> foreign.setText(""));
        foreign.setOnAction((ev) -> {
            original.setText("");
            original.requestFocus();
        });
        original.setOnMouseClicked((ev) -> original.setText(""));
        original.setOnAction((ev) -> {
            currentSession.addWord(new Word(foreign.getText(), original.getText()));

            foreign.setText("");
            original.setText("");

            currentSession.save();

            foreign.requestFocus();
        });


        addWordForm.add(foreign, 0, 0);
        addWordForm.add(original, 1, 0);

// Почему-то это не прокатывает. При нажатии на кнопки обработчики не вызываются
//        Node applyBtn = dialog.getDialogPane().lookupButton(ButtonType.APPLY);
//        Node finishBtn = dialog.getDialogPane().lookupButton(ButtonType.FINISH);
//
//        applyBtn.setOnMouseClicked(event -> {
//            System.out.println("apply");
//            currentSession.addWord(new Word(foreign.getText(), original.getText()));
//            foreign.setText("");
//            original.setText("");
//            currentSession.save();
//        });
//        finishBtn.setOnMouseClicked(event -> {
//            dialog.close();
//            System.out.println("finish");
//        });

        dialog.getDialogPane().setContent(addWordForm);
        dialog.setTitle("Add word");
        dialog.setOnCloseRequest((e) -> {
            if (!currentSession.getWords().isEmpty()) {
                sessions.add(currentSession);
                sessionsBox.getItems().add(currentSession.getName());
                sessions.get(sessions.size() - 1).setId(sessions.size() - 1);
                currentSession = null;
                dialog.close();
            }
        });
        return dialog;
    }

    private VBox getInitialPane() {
        VBox mainBox = new VBox(20);
        HBox hBox = new HBox(10);
        sessionsBox = new ComboBox<>();
        sessionsBox.setId("sessionComboBox");
        ObservableList<String> names = FXCollections.observableArrayList();
        names.add("new session");

        for (Session s : sessions) {
            names.add(s.getName());
        }

        sessionsBox.setItems(names);
        hBox.getChildren().add(sessionsBox);
        mainBox.getChildren().add(hBox);

        sessionsBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                TextField sessionName = new TextField();
                sessionName.setPromptText("session name");

                if (sessionsBox.getValue().equals("new session")) {
                    hBox.getChildren().add(sessionName);
                } else {
                    if (hBox.getChildren().size() > 1)
                        hBox.getChildren().remove(1);
                    if (mainBox.getChildren().size() > 1)
                        mainBox.getChildren().remove(1);

                    Session displayed = sessions.stream()
                            .filter(s -> s.getName().equals(sessionsBox.getValue()))
                            .collect(Collectors.toList()).get(0);

                    ScrollPane scrollPane = new ScrollPane(getSessionWordsPane(displayed));
                    mainBox.getChildren().add(scrollPane);
                }

                sessionName.setOnAction((event) -> {
                    currentSession = new Session(sessionName.getText());
                    getAddWordPane().showAndWait();
                });
            }
        });

        Button testBtn = new Button("Test");
        testBtn.setOnAction((e) -> {
            Optional<Pair<String, String>> result = getQuizSelectionDialog().showAndWait();
            if(!result.get().getKey().equals("Session")){
                getQuizStage(5, result.get().getKey()).showAndWait();
            }
            else{
                getQuizStage(5, result.get().getValue()).showAndWait();
            }
        });
        mainBox.getChildren().add(testBtn);

        return mainBox;
    }

    private GridPane getSessionWordsPane(Session session) {
        GridPane gridPane = new GridPane();
        int rowIndex = 0;
        for (Word word : session.getWords()) {
            Text foreign = new Text(word.getForeign());
            Text original = new Text(word.getOriginal());
            gridPane.addRow(rowIndex, foreign, original);
            rowIndex++;
        }
        gridPane.setHgap(20);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        return gridPane;
    }

    private Dialog<Pair<String, String>> getQuizSelectionDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Choose test format");

        ButtonType btnOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        Node okButton = dialog.getDialogPane().lookupButton(btnOk);
        okButton.setDisable(true);

        final ToggleGroup group = new ToggleGroup();

        RadioButton rbRandom = new RadioButton("Random");
        rbRandom.setToggleGroup(group);
        rbRandom.setUserData("Random");

        RadioButton rbWorst = new RadioButton("Worst");
        rbWorst.setToggleGroup(group);
        rbWorst.setUserData("Worst");

        RadioButton rbSession = new RadioButton("Session");
        rbSession.setToggleGroup(group);
        rbSession.setUserData("Session");

        ComboBox<String> sessionChoiceBox = new ComboBox<>();
        sessionChoiceBox.setOnAction((ev) -> {
            if (sessionChoiceBox.getValue() == null) {
                okButton.setDisable(true);
            } else {
                okButton.setDisable(false);
            }
        });

        ObservableList<String> names = FXCollections.observableArrayList();
        names.addAll(sessions.stream().map(Session::getName).collect(Collectors.toList()));

        group.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle old_toggle,
                 Toggle new_toggle) -> {
                    if (group.getSelectedToggle().getUserData().toString().equals("Session")) {
                        sessionChoiceBox.setItems(names);
                        sessionChoiceBox.setVisible(true);
                        okButton.setDisable(true);
                    } else {
                        sessionChoiceBox.setVisible(false);
                        sessionChoiceBox.setValue(null);
                        okButton.setDisable(false);
                    }
                });

        GridPane grid = new GridPane();
        grid.add(rbRandom, 0, 0);
        grid.add(rbWorst, 0, 1);
        grid.add(rbSession, 0, 2);
        grid.add(sessionChoiceBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> {
//            Stage quizStage;
//            int sizeOfQuiz = 10;
            if (dialogButton == btnOk) {
//                switch (group.getSelectedToggle().getUserData().toString()) {
//                    case "Random":
//                        quizStage = getQuizStage(sizeOfQuiz, "Random");
//                        break;
//                    case "Worst":
//                        quizStage = getQuizStage(sizeOfQuiz, "Worst");
//                        break;
//                    default:
//                        quizStage = getQuizStage(sizeOfQuiz, sessionChoiceBox.getValue());
//                }
                return new Pair<>(group.getSelectedToggle()
                        .getUserData().toString(), sessionChoiceBox.getValue());
            }
            return null;
        });

        return dialog;
    }

    private Stage getQuizStage(int size_, String quizType) {
        Random random = new Random();
        List<Word> allWords;
        if (quizType.equals("Random") || quizType.equals("Worst")) {
            allWords = sessions.stream().map(Session::getWords).flatMap(l -> l.stream())
                    .collect(Collectors.toList());
        } else {
            allWords = sessions.stream().filter(s -> s.getName().equals(quizType))
                    .collect(Collectors.toList()).get(0).getWords().stream().collect(Collectors.toList());

// ЧЕРТОВЩИНА КАКАЯ-ТО. КОД НИЖЕ НЕ РАБОТАЕТ
//            allWords = sessions.stream().filter(s -> s.getName().equals(quizType))
//                    .collect(Collectors.toList()).get(0).getWords();
        }

        if (quizType.equals("Random")) {
            allWords.sort((a, b) -> random.nextInt(20) - 10);
        }
        if (quizType.equals("Worst")) {
            allWords.sort((a, b) -> a.getKnowledge() - b.getKnowledge());
        }
        int size = min(size_, allWords.size());
        List<Word> testWords = allWords.subList(0, size);

        List<Pair<Integer, Integer>> wordsIds = new ArrayList<>(size);
        testWords.forEach(word -> {
            int sessionId = word.getSessionId();
            int wordId = sessions.get(sessionId).getWords().indexOf(word);
            wordsIds.add(new Pair<>(sessionId, wordId));
        });

        Stage quizStage = new Stage();
        currentIndex = 0;
        VBox vBox = new VBox();
        Text question = new Text();
        question.setText(testWords.get(0).getForeign());
        TextField answer = new TextField();
        answer.setPromptText("your answer");
        answer.setOnAction((event) -> {
            boolean correct = answer.getText().equals(testWords.get(currentIndex).getOriginal());

            Session session = sessions.get(wordsIds.get(currentIndex).getKey());
            session.updateWordKnowledge(wordsIds.get(currentIndex).getValue(), correct);
            sessions.remove((int) wordsIds.get(currentIndex).getKey());
            sessions.add(wordsIds.get(currentIndex).getKey(), session);
            session.save();

            if (!correct) {
                resentErrors.addWord(testWords.get(currentIndex));
            }
            currentIndex++;
            if (currentIndex == size) quizStage.close();
            else {
                question.setText(testWords.get(currentIndex).getForeign());
                answer.setText("");
            }
        });
        vBox.getChildren().addAll(question, answer);
        Scene scene = new Scene(vBox);
        quizStage.setScene(scene);
        return quizStage;
    }

    private int currentIndex;
}
