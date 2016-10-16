package ru.dron.activevocabe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.dron.activevocabe.controllers.QuizSelectionController;
import ru.dron.activevocabe.model.QuizProperties;
import ru.dron.activevocabe.model.SharedData;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizManager {
    private QuizProperties quizProperties;
    private SharedData sharedData;
    private Stage parentStage;

    public QuizManager(Stage parentStage, SharedData sharedData) {
        this.sharedData = sharedData;
        this.parentStage = parentStage;
    }

    public void run() {
        try {
            // Load the fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizPropertiesSelection.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select quiz settings");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            QuizSelectionController controller = loader.getController();
            controller.setAttributes(sharedData, dialogStage);

            dialogStage.showAndWait();

            if (controller.isOkPressed()) {
                System.out.println("yeeee");
                System.out.println(controller.getQuizProperties());
            } else {
                System.out.println("nooooo");
            }

        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }
}
