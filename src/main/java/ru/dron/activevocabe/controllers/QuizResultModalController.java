package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.QuizResult;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizResultModalController extends DialogController {
    private QuizResult quizResult = SharedData.getSharedData().getLastQuizResult();

    @FXML
    private AnchorPane root;
    @FXML
    private Label scoreLabel;
    @FXML
    private TableView<Word> tableView;
    @FXML
    private TableColumn<Word, String> foreignCol;
    @FXML
    private TableColumn<Word, String> translationsCol;
    @FXML
    private VBox vBox;

    @FXML
    public void initialize() {
        foreignCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getForeign()));
        translationsCol.setCellValueFactory(p ->
                new SimpleStringProperty(StringUtils
                        .join(p.getValue().getTranslations(), ", ")));

        dialogStage = new Stage();
        dialogStage.setTitle("Quiz results");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(SharedData.getSharedData().getRootStage());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);

        tableView.setItems(FXCollections.observableList(quizResult.errors
                .stream().collect(Collectors.toList())));

        double score = (1 - ((double) quizResult.errors.size()) / quizResult.testWords.size()) * 100;
        if (score != 100) {
            scoreLabel.setText(scoreLabel.getText() +
                    (new DecimalFormat("#0.00")).format(score) + "%");
        } else {
            vBox.getChildren().remove(2);
            vBox.getChildren().remove(1);
            vBox.getChildren().add(new Label("Congratulations! You made no mistakes in this test!"));
        }
    }

    @FXML
    private void repass() {
        SharedData.getSharedData().setRepassRequired(true);
        dialogStage.close();
    }

    @FXML
    private void onOkPressed() {
        SharedData.getSharedData().setRepassRequired(false);
        dialogStage.close();
    }
}
