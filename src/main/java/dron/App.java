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
import java.util.stream.Collectors;

public class App extends Application {
    public static final String testDirectory = "C:\\projects\\debug";

    private Session currentSession;
    private List<Session> sessions;
    private Stage mainStage;

    public App() {
        sessions = new ArrayList<>();
        try {
            List<File> files = Files.walk(Paths.get(testDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : files) {
                sessions.add(new Session(file));
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
        dialog.setResult(false);

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
            dialog.setResult(true);
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
        dialog.setResultConverter((e) -> {
            System.out.println(dialog.getResult());
            if (dialog.getResult()){
                sessions.add(currentSession);
            }
            return dialog.getResult();
        });
        return dialog;
    }

    private VBox getInitialPane() {
        VBox mainBox = new VBox(20);
        HBox hBox = new HBox(10);
        ComboBox<String> sessionsBox = new ComboBox<>();
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
            Optional<Pair<Integer, String>> result = getQuizSelectionDialog().showAndWait();
            System.out.println(result);
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

    private Dialog<Pair<Integer, String>> getQuizSelectionDialog() {
        Dialog<Pair<Integer, String>> dialog = new Dialog<>();
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
            if (dialogButton == btnOk) {
                return new Pair<>(Integer.parseInt(group
                        .getSelectedToggle().getUserData().toString()), sessionChoiceBox.getValue());
            }
            return null;
        });

        return dialog;
    }


}
