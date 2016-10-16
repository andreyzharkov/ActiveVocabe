package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.QuizResult;
import ru.dron.activevocabe.model.Word;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizResultModalController {
    private QuizResult quizResult;
    private Stage dialogStage;
    private boolean repassRequired = false;

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
    }

    public void setAttributes(Stage stage, QuizResult result) {
        quizResult = result;
        dialogStage = stage;

        tableView.setItems(FXCollections.observableList(result.errors
                .stream().collect(Collectors.toList())));

        double score = (1 - ((double) result.errors.size()) / result.testWords.size()) * 100;
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
        repassRequired = true;
        dialogStage.close();
    }

    @FXML
    private void onOkPressed() {
        dialogStage.close();
    }

    public boolean isRepassRequired() {
        return repassRequired;
    }
}
