package ru.dron.activevocabe.controllers;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.dron.activevocabe.model.SharedData;

/**
 * Created by Andrey on 22.10.2016.
 */
public class DialogController {
    Stage dialogStage;

    public DialogController(){
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UNIFIED);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(SharedData.getSharedData().getRootStage());
    }

    public Stage getDialogStage() {
        return dialogStage;
    }
}
