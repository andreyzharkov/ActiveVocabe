package ru.dron.activevocabe.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import ru.dron.activevocabe.model.SharedData;

/**
 * Created by Andrey on 15.10.2016.
 */
public class QuizSelectionController {
    @FXML
    private RadioButton rangomBtn;
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

    SharedData sharedData;
}
