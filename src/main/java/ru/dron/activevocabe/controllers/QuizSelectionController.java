package ru.dron.activevocabe.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.dron.activevocabe.QuizManager;
import ru.dron.activevocabe.model.QuizProperties;
import ru.dron.activevocabe.model.SharedData;

import java.util.stream.Collectors;

/**
 * Created by Andrey on 15.10.2016.
 */
public class QuizSelectionController extends DialogController {
    private ToggleGroup typeGroup;
    private ToggleGroup inputGroup;

    @FXML
    private AnchorPane root;
    @FXML
    private RadioButton randomBtn;
    @FXML
    private RadioButton ratingBtn;
    @FXML
    private RadioButton sessionBtn;
    @FXML
    private RadioButton errorsBtn;
    @FXML
    private ChoiceBox<String> sessionBox;
    @FXML
    private TextField quizSize;
    @FXML
    private RadioButton foreignInput;
    @FXML
    private RadioButton translationsInput;
    @FXML
    private Label errorMsg;
    @FXML
    private Button okButton;

    private SharedData sharedData = SharedData.getSharedData();
    private QuizManager quizManager = QuizManager.getInstance();
    private QuizProperties quizProperties;

    @FXML
    public void initialize() {
        typeGroup = new ToggleGroup();
        inputGroup = new ToggleGroup();

        randomBtn.setToggleGroup(typeGroup);
        ratingBtn.setToggleGroup(typeGroup);
        sessionBtn.setToggleGroup(typeGroup);
        errorsBtn.setToggleGroup(typeGroup);

        foreignInput.setToggleGroup(inputGroup);
        translationsInput.setToggleGroup(inputGroup);

        dialogStage.setTitle("Select quiz settings");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SharedData.CSS_PATH);
        dialogStage.setScene(scene);

        sessionBox.setItems(FXCollections
                .observableList(sharedData.getSessions().getKeys()
                        .stream().collect(Collectors.toList())));

        if (sharedData.getSessions().getResentErrors().size() > 0) {
            errorsBtn.setVisible(true);
        }
    }

    @FXML
    private void handleOk() {
        sharedData.setLastQuizProperties(quizProperties);
        quizManager.setQuizSelectionFinished(true);
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        quizManager.setQuizSelectionFinished(false);
        dialogStage.close();
    }

    @FXML
    private void onAction() {
        boolean enableOk = false;

        QuizProperties.QuizType quizType = null;
        String sessionName = null;
        int numberOfQuestions = 0;
        boolean isQuestionsOnForeign = false;

        if (randomBtn.isSelected() || ratingBtn.isSelected() || errorsBtn.isSelected()) {
            if (randomBtn.isSelected()) {
                quizType = QuizProperties.QuizType.RANDOM;
            }
            if (ratingBtn.isSelected()) {
                quizType = QuizProperties.QuizType.RATING;
            }
            if (errorsBtn.isSelected()) {
                quizType = QuizProperties.QuizType.ERRORS;
            }
            enableOk = true;
        } else {
            if (sessionBtn.isSelected() && sessionBox.getValue() != null) {
                quizType = QuizProperties.QuizType.SESSION;
                sessionName = sessionBox.getValue();
                enableOk = true;
            }
        }
        try {
            numberOfQuestions = Integer.parseInt(quizSize.getText());
        } catch (NumberFormatException ex) {
            enableOk = false;
        }
        if (foreignInput.isSelected() || translationsInput.isSelected()) {
            isQuestionsOnForeign = foreignInput.isSelected();
        } else {
            enableOk = false;
        }
        okButton.setDisable(!enableOk);
        quizProperties = new QuizProperties(quizType, numberOfQuestions, isQuestionsOnForeign, sessionName);
    }
}
