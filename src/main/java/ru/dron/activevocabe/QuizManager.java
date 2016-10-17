package ru.dron.activevocabe;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.controllers.QuizFormController;
import ru.dron.activevocabe.controllers.QuizResultModalController;
import ru.dron.activevocabe.controllers.QuizSelectionController;
import ru.dron.activevocabe.model.*;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizManager {
    private QuizProperties quizProperties;
    private SharedData sharedData = SharedData.getSharedData();
    private Stage parentStage;

    public QuizManager(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void run() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizPropertiesSelection.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Select quiz settings");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            QuizSelectionController controller = loader.getController();
            controller.setAttributes(dialogStage);

            dialogStage.showAndWait();

            if (controller.isOkPressed()) {
                showQuizDialog(controller.getQuizProperties());
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    private void showQuizDialog(QuizProperties properties) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizForm.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Quiz");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            QuizFormController controller = loader.getController();
            controller.setAttributes(dialogStage, properties);

            dialogStage.showAndWait();

            if (controller.wasEndedNormally()) {
                showQuizResult(controller.getResult());
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    private void showQuizDialog(QuizResult quizResult) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizForm.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Quiz");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            QuizFormController controller = loader.getController();
            controller.setAttributes(dialogStage, quizResult);

            dialogStage.showAndWait();

            if (controller.wasEndedNormally()) {
                showQuizResult(controller.getResult());
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    private void showQuizResult(QuizResult quizResult) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizResults.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Quiz results");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            QuizResultModalController controller = loader.getController();
            controller.setAttributes(dialogStage, quizResult);

            dialogStage.showAndWait();

            if (controller.isRepassRequired()) {
                showQuizDialog(quizResult);
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }
}
