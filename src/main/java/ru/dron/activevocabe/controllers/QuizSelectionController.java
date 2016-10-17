package ru.dron.activevocabe.controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.dron.activevocabe.model.QuizProperties;
import ru.dron.activevocabe.model.SharedData;

import java.util.stream.Collectors;

/**
 * Created by Andrey on 15.10.2016.
 */
public class QuizSelectionController {
    private ToggleGroup typeGroup;
    private ToggleGroup inputGroup;

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
    private Stage stage;
    private QuizProperties quizProperties;

    private boolean wasOkPressed = false;

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
    }

    public void setAttributes(Stage stage) {
        this.stage = stage;

        sessionBox.setItems(FXCollections
                .observableList(sharedData.getSessions().getKeys()
                        .stream().collect(Collectors.toList())));

        if (sharedData.getSessions().getResentErrors().size() > 0) {
            errorsBtn.setVisible(true);
        }
    }

    public boolean isOkPressed() {
        return wasOkPressed;
    }

    public QuizProperties getQuizProperties() {
        return quizProperties;
    }

    @FXML
    private void handleOk() {
        wasOkPressed = true;
        stage.close();
    }

    @FXML
    private void handleCancel() {
        stage.close();
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
