package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static ru.dron.activevocabe.model.Word.TRANSLATION_SEPARATOR;

/**
 * Created by Andrey on 30.10.2016.
 */
public class WordsCheckController extends DialogController {
    private SharedData sharedData = SharedData.getSharedData();

    @FXML
    private AnchorPane root;
    @FXML
    private TableView<TWord> tableView;
    @FXML
    private TableColumn<TWord, String> foreignCol;
    @FXML
    private TableColumn<TWord, String> translationsCol;

    private ObservableList<TWord> words;

    public void setWords(ObservableList<TWord> words) {
        this.words = words;
        tableView.setItems(words);
        MenuItem removeLine = new MenuItem("remove this word");
        removeLine.setOnAction(event -> {
            tableView.getItems().remove(tableView.getSelectionModel().getSelectedItem());
        });
        removeLine.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN));
        tableView.setContextMenu(new ContextMenu(removeLine));
    }

    @FXML
    public void initialize() {
        dialogStage = new Stage();
        dialogStage.setTitle("Check words");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(sharedData.getRootStage());

        foreignCol.setCellValueFactory(new PropertyValueFactory<>("foreign"));
        foreignCol.setCellFactory(TextFieldTableCell.forTableColumn());
        translationsCol.setCellValueFactory(new PropertyValueFactory<>("translation"));
        translationsCol.setCellFactory(TextFieldTableCell.forTableColumn());
        foreignCol.setOnEditCommit((t) -> t.getTableView().getItems()
                .get(t.getTablePosition().getRow()).setForeign(t.getNewValue()));
        translationsCol.setOnEditCommit((t) -> t.getTableView().getItems()
                .get(t.getTablePosition().getRow()).setTranslation(t.getNewValue()));

        tableView.setEditable(true);
        if (words != null) {
            tableView.setItems(words);
        }

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
    }

    @FXML
    private void onCancel() {
        dialogStage.close();
    }

    @FXML
    private void onOk() {
        sharedData.getSessions().addAll(sharedData.getCurrentSession(),
                tableView.getItems().stream().map(TWord::getWord).collect(Collectors.toList()));
        sharedData.saveSession(sharedData.getCurrentSession());
        dialogStage.close();
    }

    public static class TWord {
        private final SimpleStringProperty foreign;
        private final SimpleStringProperty translation;

        public TWord(Word w) {
            this.foreign = new SimpleStringProperty(w.getForeign());
            this.translation = new SimpleStringProperty(StringUtils
                    .join(w.getTranslations(), TRANSLATION_SEPARATOR));
        }

        public Word getWord() {
            String foreign = getForeign().replaceAll("(^\\s*|\\s*$)", "");
            List<String> translations = Arrays.stream(getTranslation().split(TRANSLATION_SEPARATOR))
                    .map(s -> s.replaceAll("(^\\s*|\\s*$)", ""))
                    .filter(s -> !s.equals(""))
                    .collect(Collectors.toList());
            return new Word(foreign, translations);
        }

        public String getForeign() {
            return foreign.get();
        }

        public void setForeign(String foreign_) {
            foreign.set(foreign_);
        }

        public String getTranslation() {
            return translation.get();
        }

        public void setTranslation(String tr) {
            translation.set(tr);
        }
    }
}