package dron;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class App extends Application {
    public static final String testDirectory = "C:\\projects\\debug";

    private static Session currentSession;
    private static List<Session> sessions;
    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
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


        mainStage = primaryStage;
        mainStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        TextArea newNote = new TextArea("Enter your note here");


        StackPane root = new StackPane();
        root.getChildren().add(newNote);
        root.getChildren().add(btn);

        Scene scene = new Scene(getInitialPane(), 300, 250);

        mainStage.setTitle("Hello World!");
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static GridPane getAddWordPane() {
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
        Button addWordButton = new Button("add");
        addWordButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currentSession.addWord(new Word(foreign.getText(), original.getText()));
                foreign.setText("");
                original.setText("");

                currentSession.save();
            }
        });
        addWordForm.add(addWordButton, 1, 1);
        Button backButton = new Button("back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                sessions.add(currentSession);
                mainStage.setScene(new Scene(getInitialPane(), 300, 250));
            }
        });
        addWordForm.add(backButton, 1, 2);
        return addWordForm;
    }

    private static VBox getInitialPane() {
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
                TextField sessionName = new TextField("New session");

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
                    mainBox.getChildren().add(getSessionWordsPane(displayed));
                }

                sessionName.setOnAction((event) -> {
                    currentSession = new Session(sessionName.getText());
                    mainStage.setScene(new Scene(getAddWordPane(), 300, 250));
                });
            }
        });

        return mainBox;
    }

    private static GridPane getSessionWordsPane(Session session) {
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
}
