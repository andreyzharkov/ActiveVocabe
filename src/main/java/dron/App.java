package dron;

import javafx.application.Application;
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class App extends Application {
    public static final String testDirectory = "C:\\projects\\debug";

    private static Session currentSession;
    private static Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
        Session t = new Session("test_save");
        t.addWord(new Word("f", "r", "fh", "rh"));
        t.save();


        mainStage = primaryStage;
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

        Scene scene = new Scene(getSessionHbox(), 300, 250);

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

        TextField foreign = new TextField("foreign");
        foreign.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                foreign.setText("");
            }
        });
        TextField native_ = new TextField("native");
        native_.setOnMouseClicked((ev) ->{native_.setText("");});
        TextField foreign_hint = new TextField("foreign hint");
        TextField native_hint = new TextField("native hint");
        foreign_hint.setOnMouseClicked((ev) ->{foreign_hint.setText("");});
        native_hint.setOnMouseClicked((ev) ->{native_hint.setText("");});



        addWordForm.add(foreign, 0, 0);
        addWordForm.add(native_, 1, 0);
        addWordForm.add(foreign_hint, 0, 1);
        addWordForm.add(native_hint, 1, 1);
        Button addWordButton = new Button("add");
        addWordButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                currentSession.addWord(new Word(foreign.getText(), native_.getText(),
                        foreign_hint.getText(), native_hint.getText()));
                foreign.setText("");
                native_.setText("");
                foreign_hint.setText("");
                native_hint.setText("");

                System.out.println("before save");
                currentSession.print();
                currentSession.save();
                currentSession.print();
            }
        });
        addWordForm.add(addWordButton, 1, 2);
        return addWordForm;
    }

    private static GridPane getSessionViewPane() {
        GridPane grid = new GridPane();
        grid.setHgap(50);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Text foreign = new Text("foreign");
        Text translation = new Text("иностранный");

        Text foreign_hint = new Text("adjective");
        Text translation_hint = new Text("прилагательное");

        VBox left = new VBox();
        VBox right = new VBox();
        left.getChildren().addAll(foreign, foreign_hint);
        right.getChildren().addAll(translation, translation_hint);

        grid.add(left, 0, 0);
        grid.add(right, 1, 0);
        //grid.addRow(1, foreign, translation);

        return grid;
    }

    private static HBox getSessionHbox() {
        HBox sessionBox = new HBox(10);
        ChoiceBox sessions = new ChoiceBox();
        ObservableList names = FXCollections.observableArrayList();
        names.add("new session");
        names.add("fffff");

        try {
            List<File> files = Files.walk(Paths.get(testDirectory))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            for (File file : files) {
                names.add(file.getName());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        sessions.setItems(names);
        sessionBox.getChildren().add(sessions);

        sessions.getSelectionModel().selectedIndexProperty().addListener(
                (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
                    TextField sessionName = new TextField("New session");
                    sessionBox.getChildren().add(sessionName);
                    sessionName.setOnAction((event) -> {
                        currentSession = new Session(sessionName.getText());
                        mainStage.setScene(new Scene(getAddWordPane(), 300, 250));
                    });
                }
        );

        return sessionBox;
    }
}
