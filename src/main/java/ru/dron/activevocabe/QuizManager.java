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
    private SharedData sharedData;
    private Set<Word> resentErrors;
    private Stage parentStage;

    public QuizManager(Stage parentStage, SharedData sharedData) {
        this.sharedData = sharedData;
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
            controller.setAttributes(sharedData, dialogStage);

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
//        Sessions sessions = sharedData.getSessions();
//        Dialog<Integer> dialog = new Dialog<>();
//
//        VBox vBox = new VBox(10);
//        vBox.setAlignment(Pos.CENTER);
//        vBox.setOpaqueInsets(new Insets(5, 5, 5, 5));
//
//        Text question = new Text();
//        TextField answer = new TextField();
//        answer.setPromptText("Your answer");
//
//        List<Word> questionList = new ArrayList<>();
//
//        if (properties.getQuizType().equals(QuizProperties.QuizType.RANDOM)) {
//            Random random = new Random();
//            questionList = sessions.getValues().stream().flatMap(Set::stream)
//                    .sorted((o1, o2) -> random.nextInt(11) - 5)
//                    .limit(properties.getNumberOfQuestions())
//                    .collect(Collectors.toList());
//        }
//        if (properties.getQuizType().equals(QuizProperties.QuizType.RATING)) {
//            questionList = sessions.getValues().stream().flatMap(Set::stream)
//                    .sorted(Word.getKnowledgeComparator())
//                    .limit(properties.getNumberOfQuestions())
//                    .collect(Collectors.toList());
//        }
//        if (properties.getQuizType().equals(QuizProperties.QuizType.SESSION)) {
//            questionList = sessions.get(properties.getSessionName())
//                    .stream()
//                    .sorted(Word.getKnowledgeComparator())
//                    .limit(properties.getNumberOfQuestions())
//                    .collect(Collectors.toList());
//        }
//        if (properties.getQuizType().equals(QuizProperties.QuizType.ERRORS)) {
//            questionList = resentErrors.stream().collect(Collectors.toList());
//        }
//
//        final List<Word> testList = questionList;
//
//        correctAnswers = 0;
//        currentIndex = 0;
//        resentErrors = Collections.synchronizedSet(new HashSet<>());
//
//        if (properties.isQuestionsOnForeign()) {
//            question.setText(testList.get(currentIndex).getForeign());
//        } else {
//            question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
//        }
//
//        answer.setOnAction((e) -> {
//            boolean correct;
//            if (properties.isQuestionsOnForeign()) {
//                correct = testList.get(currentIndex).getTranslations().contains(answer.getText());
//            } else {
//                correct = testList.get(currentIndex).getForeign().equals(answer.getText());
//            }
//
//            if (correct) {
//                correctAnswers++;
//            } else {
//                resentErrors.add(testList.get(currentIndex));
//            }
//
//            sessions.getWord(testList.get(currentIndex)).updateKnowledge(correct);
//            sharedData.saveSession(sessions.getKeyOf(testList.get(currentIndex)));
//
//            currentIndex++;
//            if (currentIndex == testList.size()) {
//                dialog.setResult(correctAnswers);
//                dialog.close();
//                showQuizResult(testList.size());
//            } else {
//                if (properties.isQuestionsOnForeign()) {
//                    question.setText(testList.get(currentIndex).getForeign());
//                } else {
//                    question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
//                }
//                answer.setText("");
//            }
//        });
//
//        vBox.getChildren().addAll(question, answer);
//        dialog.getDialogPane().setContent(vBox);
//        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
//        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
//
//        dialog.showAndWait();
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
            controller.setAttributes(sharedData, dialogStage, properties);

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
            controller.setAttributes(sharedData, dialogStage, quizResult);

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
//        int wordsInQuiz = quizResult.testWords.size();
//        int correctAnswers = wordsInQuiz - quizResult.errors.size();
//        resentErrors = quizResult.errors;
//
//        Text persentage = new Text((new DecimalFormat("#.00")).format(
//                ((double) correctAnswers) / wordsInQuiz * 100) + "%");
//
//        GridPane grid = new GridPane();
//        grid.setHgap(10);
//        grid.setVgap(10);
//        grid.setOpaqueInsets(new Insets(10, 10, 10, 10));
//
//        grid.add(persentage, 0, 0);
//        grid.add(getErrorsViewPane(), 0, 1, 10, 10);
//
//        Dialog dialog = new Dialog();
//        dialog.getDialogPane().setContent(grid);
//        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
//        dialog.showAndWait();
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
