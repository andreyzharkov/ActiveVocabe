package ru.dron.activevocabe.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 30.10.2016.
 */
public class WordsCheckController extends DialogController {
    SharedData sharedData = SharedData.getSharedData();
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
                tableView.getItems().stream().map(w -> w.getWord()).collect(Collectors.toList()));
        dialogStage.close();
    }

    public static class TWord extends Word {
        private final SimpleStringProperty foreign;
        private final SimpleStringProperty translation;

        public TWord(Word w) {
            super(w.getForeign(), w.getTranslations());
            this.foreign = new SimpleStringProperty(w.getForeign());
            this.translation = new SimpleStringProperty(StringUtils
                    .join(w.getTranslations(), ", "));
        }

        public Word getWord() {
            return new Word(super.foreign, super.translations);
        }

        public String getForeign() {
            return foreign.get();
        }

        public void setForeign(String foreign_) {
            super.foreign = foreign_;
            foreign.set(foreign_);
        }

        public String getTranslation() {
            return translation.get();
        }

        public void setTranslation(String tr) {
            super.translations = Arrays.asList(tr.split(","));
            super.translations.forEach((s) -> Arrays.stream(s.split("\\s"))
                    .filter((s0) -> !s0.equals("")).reduce((s1, s2) -> s1 + " " + s2));
            translation.set(tr);
        }
    }
}
