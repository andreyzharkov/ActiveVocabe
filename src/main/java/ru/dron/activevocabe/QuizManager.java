package ru.dron.activevocabe;

import javafx.fxml.FXMLLoader;
import ru.dron.activevocabe.controllers.DialogController;
import ru.dron.activevocabe.model.*;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizManager {
    private SharedData sharedData = SharedData.getSharedData();
    private boolean quizSelectionFinished = false;
    private boolean quizFinished = false;

    private static QuizManager quizManager;

    public static QuizManager getInstance() {
        if (quizManager == null) {
            quizManager = new QuizManager();
        }
        return quizManager;
    }

    public void run() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizPropertiesSelection.fxml"));
            loader.load();

            ((DialogController) loader.getController()).getDialogStage().showAndWait();

            if (quizSelectionFinished) {
                showQuizDialog();
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    public void run(String session){
        setQuizSelectionFinished(true);
        sharedData.setLastQuizProperties(
                new QuizProperties(
                        QuizProperties.QuizType.SESSION, Integer.MAX_VALUE, false, session
                )
        );
        showQuizDialog();
    }

    private void showQuizDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizForm.fxml"));
            loader.load();

            ((DialogController) loader.getController()).getDialogStage().showAndWait();

            if (quizFinished) {
                showQuizResult();
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    private void showQuizResult() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuizResults.fxml"));
            loader.load();

            ((DialogController) loader.getController()).getDialogStage().showAndWait();

            if (sharedData.isRepassRequired()) {
                showQuizDialog();
            }
        } catch (Exception e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            System.exit(1500);
        }
    }

    public void setQuizSelectionFinished(boolean finished) {
        quizSelectionFinished = finished;
    }

    public void setQuizFinished(boolean finished) {
        quizFinished = finished;
    }
}
