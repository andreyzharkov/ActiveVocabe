package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.Word;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 15.10.2016.
 */
public class SessionViewController {
    @FXML
    private Label label;
    @FXML
    private TableView<Word> tableView;
    @FXML
    private TableColumn<Word, String> foreignCol;
    @FXML
    private TableColumn<Word, String> translationsCol;

    @FXML
    public void initialize() {
        foreignCol.setCellValueFactory(p ->
                new SimpleStringProperty(p.getValue().getForeign()));
        translationsCol.setCellValueFactory(p ->
                new SimpleStringProperty(StringUtils
                        .join(p.getValue().getTranslations(), ", ")));
    }

    public void update(Set<Word> words) {
        ObservableList<Word> items = FXCollections.observableList(words
                .stream().collect(Collectors.toList()));
        tableView.setItems(items);

        label.setText(label.getText() + "OOOOOOOOO");
    }
}
