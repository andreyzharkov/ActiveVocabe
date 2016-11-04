package ru.dron.activevocabe.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.dron.activevocabe.FileTransformer;
import ru.dron.activevocabe.model.SharedData;

import java.io.File;

/**
 * Created by Andrey on 30.10.2016.
 */
public class FileReaderController extends DialogController {
    private SharedData sharedData = SharedData.getSharedData();

    @FXML
    AnchorPane root;
    @FXML
    CheckBox removeNumbers;
    @FXML
    CheckBox removeBrackets;
    @FXML
    CheckBox removePunctuation;
    @FXML
    TextField ftSepString;
    @FXML
    TextField trSepString;
    @FXML
    Text filePath;
    @FXML
    TextField encoding;

    @FXML
    public void initialize() {
        dialogStage = new Stage();
        dialogStage.setTitle("");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(sharedData.getRootStage());
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
    }

    @FXML
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(getDialogStage());
        if (selectedFile != null) {
            filePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onOkPressed() {
        System.out.println(trSepString.getText());
        FileTransformer.getInstance().setProperties(filePath.getText(),
                ftSepString.getText(), trSepString.getText(),
                removeNumbers.isSelected(), removeBrackets.isSelected(),
                removePunctuation.isSelected(), encoding.getText());
        getDialogStage().close();
    }

    @FXML
    private void onCancelPressed() {
        FileTransformer.getInstance().setProperties(null,
                ftSepString.getText(), trSepString.getText(),
                removeNumbers.isSelected(), removeBrackets.isSelected(),
                removePunctuation.isSelected(), "");
        getDialogStage().close();
    }
}
