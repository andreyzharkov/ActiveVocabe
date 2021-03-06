package ru.dron.activevocabe.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;
import ru.dron.activevocabe.QuizManager;
import ru.dron.activevocabe.model.QuizProperties;
import ru.dron.activevocabe.model.QuizResult;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Andrey on 16.10.2016.
 */
public class QuizFormController extends DialogController {
    private SharedData sharedData = SharedData.getSharedData();
    private QuizManager quizManager = QuizManager.getInstance();
    private Set<Word> resentErrors;
    private QuizProperties properties = SharedData.getSharedData().getLastQuizProperties();

    private int currentIndex;
    private List<Word> testList;

    @FXML
    private AnchorPane root;
    @FXML
    private Label question;
    @FXML
    private TextField answer;
    @FXML
    private Button nextButton;

    @FXML
    public void initialize() {
        currentIndex = 0;
        resentErrors = Collections.synchronizedSet(new HashSet<>());

        dialogStage.setTitle("Quiz");
        Scene scene = new Scene(root, 400, 200);
        scene.getStylesheets().add(SharedData.CSS_PATH);
        dialogStage.setScene(scene);

        List<Word> questionList = new ArrayList<>();

        if (sharedData.isRepassRequired()) {
            questionList = sharedData.getLastQuizResult().testWords;
        } else {
            if (properties.getQuizType().equals(QuizProperties.QuizType.RANDOM)) {
                Random random = new Random();
                questionList = sharedData.getSessions().getValues().stream().flatMap(Set::stream)
                        .sorted((o1, o2) -> random.nextInt(11) - 5)
                        .limit(properties.getNumberOfQuestions())
                        .collect(Collectors.toList());
            }
            if (properties.getQuizType().equals(QuizProperties.QuizType.RATING)) {
                questionList = sharedData.getSessions().getValues().stream().flatMap(Set::stream)
                        .sorted(Word.getKnowledgeComparator())
                        .limit(properties.getNumberOfQuestions())
                        .collect(Collectors.toList());
            }
            if (properties.getQuizType().equals(QuizProperties.QuizType.SESSION)) {
                questionList = sharedData.getSessions().get(properties.getSessionName())
                        .stream()
                        .sorted(Word.getKnowledgeComparator())
                        .limit(properties.getNumberOfQuestions())
                        .collect(Collectors.toList());
            }
            if (properties.getQuizType().equals(QuizProperties.QuizType.ERRORS)) {
                questionList = sharedData.getSessions().getResentErrors()
                        .stream().collect(Collectors.toList());
            }
        }


        testList = questionList;

        assert(testList.size() > 0);
        dialogStage.setTitle("Question 1/" + testList.size());
        if (properties.isQuestionsOnForeign()) {
            question.setText(testList.get(currentIndex).getForeign());
        } else {
            question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
        }
    }

    @FXML
    private void onNextPressed() {
        boolean correct;
        if (properties.isQuestionsOnForeign()) {
            correct = testList.get(currentIndex).getTranslations().contains(answer.getText());
        } else {
            correct = testList.get(currentIndex).getForeign().equals(answer.getText());
        }

        if (!correct) {
            resentErrors.add(testList.get(currentIndex));
            sharedData.getSessions().addError(testList.get(currentIndex));
        }

        sharedData.getSessions().getWord(testList.get(currentIndex)).updateKnowledge(correct);
        sharedData.saveSession(sharedData.getSessions().getKeyOf(testList.get(currentIndex)));

        currentIndex++;
        if (currentIndex == testList.size()) {
            sharedData.setLastQuizResult(new QuizResult(testList, resentErrors, properties));
            quizManager.setQuizFinished(true);
            dialogStage.close();
        } else {
            dialogStage.setTitle("Question " + (currentIndex + 1) + "/" + testList.size());
            if (currentIndex == testList.size() - 1) {
                nextButton.setText("Finish");
            }
            if (properties.isQuestionsOnForeign()) {
                question.setText(testList.get(currentIndex).getForeign());
            } else {
                question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
            }
            answer.setText("");
        }
    }
}
