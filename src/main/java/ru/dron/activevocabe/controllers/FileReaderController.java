package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.FileTransformer;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.io.File;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 30.10.2016.
 */
public class FileReaderController extends DialogController {
    SharedData sharedData = SharedData.getSharedData();

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
        FileTransformer.getInstance().setProperties(filePath.getText(), sharedData.getCurrentSession(),
                ftSepString.getText(), trSepString.getText(),
                removeNumbers.isSelected(), removeBrackets.isSelected(), removePunctuation.isSelected());
        getDialogStage().close();

//        TableView<Word> tableView = new TableView<>();
//        TableColumn<Word, String> foreign = new TableColumn<>("Foreign");
//        TableColumn<Word, String> translations = new TableColumn<>("Translations");
//
//        foreign.setCellValueFactory(p ->
//                new SimpleStringProperty(p.getValue().getForeign()));
//        translations.setCellValueFactory(p ->
//                new SimpleStringProperty(StringUtils
//                        .join(p.getValue().getTranslations(), ", ")));
//        foreign.setCellFactory(TextFieldTableCell.forTableColumn());
//        translations.setCellFactory(TextFieldTableCell.forTableColumn());
//
//        tableView.getColumns().addAll(foreign, translations);
//        tableView.setItems(ol);
//        tableView.setEditable(true);
//
//        AnchorPane anchorPane = new AnchorPane();
//        anchorPane.getChildren().add(tableView);
//        anchorPane.setBottomAnchor(tableView, 50.0);
//        anchorPane.setTopAnchor(tableView, 0.0);
//        anchorPane.setLeftAnchor(tableView, 0.0);
//        anchorPane.setRightAnchor(tableView, 0.0);
//
//        Button addAll = new Button("Add all words");
//        Button cancel = new Button("Cancel");
//
//        Stage checkWordsStage = new Stage();
//        Scene scene = new Scene(anchorPane, 800, 600);
//        scene.getStylesheets().add("/css/styles.css");
//
//        checkWordsStage.setScene(scene);
//        checkWordsStage.show();
    }

    @FXML
    private void onCancelPressed() {
        FileTransformer.getInstance().setProperties(null, sharedData.getCurrentSession(),
                ftSepString.getText(), trSepString.getText(),
                removeNumbers.isSelected(), removeBrackets.isSelected(), removePunctuation.isSelected());
        getDialogStage().close();
    }
}
