package ru.dron.activevocabe;

import javafx.fxml.FXMLLoader;
import org.apache.commons.lang3.StringUtils;
import com.google.common.base.CaseFormat;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.dron.activevocabe.controllers.RootPaneController;
import ru.dron.activevocabe.controllers.SessionViewController;
import ru.dron.activevocabe.model.Sessions;
import ru.dron.activevocabe.model.SharedData;
import ru.dron.activevocabe.model.Word;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class App extends Application {
    private String rootDirectory;
    private Sessions sessions;
    private Set<Word> resentErrors;

    private SharedData sharedData;

    private TreeView<String> treeView;
    private HBox root;

    @Override
    public void start(Stage primaryStage) {
        Stage mainStage = primaryStage;

        HBox hBox = new HBox();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RootPane.fxml"));
            hBox = loader.load();
            RootPaneController controller = loader.getController();
            sharedData = controller.getSharedData();
            controller.setStage(primaryStage);

            rootDirectory = sharedData.getRootDirectory();
            sessions = sharedData.getSessions();
            resentErrors = sharedData.getResentErrors();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1000);
        }
        root = hBox;

        Scene scene = new Scene(root, 800, 600);
        String cssPath = "/css/styles.css";
        scene.getStylesheets().add(cssPath);

        mainStage.setTitle("Active Vocabe");
        mainStage.setScene(scene);
        mainStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private VBox getErrorsViewPane() {
        if (resentErrors.size() == 0) {
            Text congratulation = new Text("Congratulations! You made no mistakes in this test!");
            VBox vBox = new VBox();
            vBox.getChildren().add(congratulation);
            return vBox;
        }

        TableView<Word> table = new TableView<>();

        final Label label = new Label("Your errors:");
        label.setFont(new Font("Arial", 20));

        TableColumn<Word, String> foreignCol = new TableColumn<>("Foreign");
        TableColumn<Word, String> translationsCol = new TableColumn<>("Translations");

        table.getColumns().addAll(foreignCol, translationsCol);

        foreignCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Word, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Word, String> p) {
                // p.getValue() returns the Person instance for a particular TableView row
                return new SimpleStringProperty(p.getValue().getForeign());
            }
        });

        translationsCol.setCellValueFactory((p) -> {
            return new SimpleStringProperty(StringUtils.join(p.getValue().getTranslations(), ", "));
        });

        ObservableList<Word> items = FXCollections.observableList(resentErrors
                .stream().collect(Collectors.toList()));
        table.setItems(items);

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, table);
        return vBox;
    }

    private void showAddWordPane(String session) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setOpaqueInsets(new Insets(10, 10, 10, 10));

        TextField foreign = new TextField();
        foreign.setPromptText("foreign");
        TextField translation = new TextField();
        translation.setPromptText("tr-s, sep is \';\'");
        foreign.setOnAction((e) -> translation.requestFocus());
        translation.setOnAction((e) -> {
            List<String> tr = new ArrayList<>();
            Arrays.asList(translation.getText().split(";")).forEach(t -> {
                if (!t.equals("") && !t.matches("\\s")) {
                    tr.add(StringUtils.join(Arrays.stream(t.split("\\s"))
                            .filter(s -> !s.equals(""))
                            .collect(Collectors.toList()), " "));
                }
            });
            System.out.println(tr);
            sessions.get(session).add(new Word(foreign.getText(), tr));
            sharedData.saveSession(session);
            foreign.clear();
            translation.clear();
            foreign.requestFocus();
        });

        grid.addRow(0, foreign, translation);
        Dialog<Boolean> dialog = new Dialog<>();
        DialogPane dialogPane = new DialogPane();
        dialogPane.setContent(grid);
        dialog.setDialogPane(dialogPane);
        dialog.setTitle("Add new word to session " + session);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.showAndWait();
    }

    private void showSelectQuizTypeDialog() {
        Dialog<QuizProperties> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setOpaqueInsets(new Insets(10, 10, 10, 10));

        ToggleGroup radioGroup = new ToggleGroup();
        radioGroup.setUserData(null);
        RadioButton randomBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.RANDOM.toString().toLowerCase()));
        RadioButton ratingBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.RATING.toString().toLowerCase()));
        RadioButton sessionBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.SESSION.toString().toLowerCase()));
        RadioButton errorsBtn = new RadioButton(
                CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                        QuizType.ERRORS.toString().toLowerCase()));
        ComboBox<String> sessionChoiceBox = new ComboBox<>(FXCollections
                .observableList(sessions.getKeys()
                        .stream().collect(Collectors.toList())));
        sessionChoiceBox.setOnAction((e) -> {
            if (sessionChoiceBox.getValue() != null) {
                radioGroup.setUserData(sessionBtn.getText());
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            }
        });
        sessionChoiceBox.setValue(null);
        sessionChoiceBox.setDisable(true);

        randomBtn.setToggleGroup(radioGroup);
        ratingBtn.setToggleGroup(radioGroup);
        sessionBtn.setToggleGroup(radioGroup);
        errorsBtn.setToggleGroup(radioGroup);

        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        randomBtn.setOnAction((e) -> {
            radioGroup.setUserData(randomBtn.getText());
            sessionChoiceBox.setValue(null);
            sessionChoiceBox.setDisable(true);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });
        ratingBtn.setOnAction((e) -> {
            radioGroup.setUserData(ratingBtn.getText());
            sessionChoiceBox.setValue(null);
            sessionChoiceBox.setDisable(true);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });
        sessionBtn.setOnAction((e) -> {
            radioGroup.setUserData("");
            sessionChoiceBox.setDisable(false);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
        });
        errorsBtn.setOnAction((e) -> {
            radioGroup.setUserData(ratingBtn.getText());
            sessionChoiceBox.setValue(null);
            sessionChoiceBox.setDisable(true);
            dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
        });

        Label testSizeLabel = new Label("Size of test: ");
        TextField testSizeInput = new TextField("0");
        testSizeInput.setOnAction((e) -> {
            if (dialog.getDialogPane().lookupButton(ButtonType.OK).isDisable()
                    && sessionChoiceBox.getValue() != null) {
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
            }
            try {
                Integer.parseInt(testSizeInput.getText());
            } catch (NumberFormatException ex) {
                dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
            }
        });

        VBox radios = new VBox(randomBtn, ratingBtn, sessionBtn);
        if (!resentErrors.isEmpty()) {
            radios.getChildren().add(errorsBtn);
        }

        RadioButton foreignBtn = new RadioButton("Foreign input");
        RadioButton translationBtn = new RadioButton("Translation input");
        ToggleGroup languageGroup = new ToggleGroup();
        foreignBtn.setToggleGroup(languageGroup);
        translationBtn.setToggleGroup(languageGroup);
        foreignBtn.setSelected(true);
        foreignBtn.setUserData(true);
        translationBtn.setUserData(false);

        gridPane.add(radios, 0, 0, 1, 4);
        gridPane.add(sessionChoiceBox, 1, 4);
        gridPane.add(testSizeLabel, 0, 5);
        gridPane.add(testSizeInput, 1, 5, 2, 1);
        gridPane.add(foreignBtn, 0, 6, 2, 1);
        gridPane.add(translationBtn, 0, 7, 2, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter((btn) -> {
            if (btn.equals(ButtonType.CANCEL)) return null;
            String resultType = radioGroup.getUserData().toString();
            int testSize = Integer.parseInt(testSizeInput.getText());

            if (resultType.equals(randomBtn.getText())) {
                return new QuizProperties(QuizType.RANDOM, testSize,
                        (boolean) languageGroup.getSelectedToggle().getUserData());
            }
            if (resultType.equals(ratingBtn.getText())) {
                return new QuizProperties(QuizType.RATING, testSize,
                        (boolean) languageGroup.getSelectedToggle().getUserData());
            }
            if (resultType.equals(errorsBtn.getText())) {
                return new QuizProperties(QuizType.ERRORS, testSize,
                        (boolean) languageGroup.getSelectedToggle().getUserData());
            }

            return new QuizProperties(QuizType.SESSION, testSize,
                    (boolean) languageGroup.getSelectedToggle().getUserData(),
                    sessionChoiceBox.getValue());
        });

        Optional<QuizProperties> result = dialog.showAndWait();
        if (result.isPresent()) {
            showQuizDialog(result.get());
        }
    }

    private void showQuizDialog(QuizProperties properties) {
        Dialog<Integer> dialog = new Dialog<>();

        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER);
        vBox.setOpaqueInsets(new Insets(5, 5, 5, 5));

        Text question = new Text();
        TextField answer = new TextField();
        answer.setPromptText("Your answer");

        List<Word> questionList = new ArrayList<>();

        if (properties.getQuizType().equals(QuizType.RANDOM)) {
            Random random = new Random();
            questionList = sessions.getValues().stream().flatMap(Set::stream)
                    .sorted((o1, o2) -> random.nextInt(11) - 5)
                    .limit(properties.getNumberOfQuestions())
                    .collect(Collectors.toList());
        }
        if (properties.getQuizType().equals(QuizType.RATING)) {
            questionList = sessions.getValues().stream().flatMap(Set::stream)
                    .sorted(Word.getKnowledgeComparator())
                    .limit(properties.getNumberOfQuestions())
                    .collect(Collectors.toList());
        }
        if (properties.getQuizType().equals(QuizType.SESSION)) {
            questionList = sessions.get(properties.getSessionName())
                    .stream()
                    .sorted(Word.getKnowledgeComparator())
                    .limit(properties.getNumberOfQuestions())
                    .collect(Collectors.toList());
        }
        if (properties.getQuizType().equals(QuizType.ERRORS)) {
            questionList = resentErrors.stream().collect(Collectors.toList());
        }

        final List<Word> testList = questionList;

        correctAnswers = 0;
        currentIndex = 0;
        resentErrors = Collections.synchronizedSet(new HashSet<>());

        if (properties.isQuestionsOnForeign()) {
            question.setText(testList.get(currentIndex).getForeign());
        } else {
            question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
        }

        answer.setOnAction((e) -> {
            boolean correct;
            if (properties.isQuestionsOnForeign()) {
                correct = testList.get(currentIndex).getTranslations().contains(answer.getText());
            } else {
                correct = testList.get(currentIndex).getForeign().equals(answer.getText());
            }

            if (correct) {
                correctAnswers++;
            } else {
                resentErrors.add(testList.get(currentIndex));
            }

            sessions.getWord(testList.get(currentIndex)).updateKnowledge(correct);
            sharedData.saveSession(sessions.getKeyOf(testList.get(currentIndex)));

            currentIndex++;
            if (currentIndex == testList.size()) {
                dialog.setResult(correctAnswers);
                dialog.close();
                showQuizResult(testList.size());
            } else {
                if (properties.isQuestionsOnForeign()) {
                    question.setText(testList.get(currentIndex).getForeign());
                } else {
                    question.setText(StringUtils.join(testList.get(currentIndex).getTranslations(), ", "));
                }
                answer.setText("");
            }
        });

        vBox.getChildren().addAll(question, answer);
        dialog.getDialogPane().setContent(vBox);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

        dialog.showAndWait();
    }

    private void showQuizResult(int wordsInQuiz) {
        Text persentage = new Text((new DecimalFormat("#.00")).format(
                ((double) correctAnswers) / wordsInQuiz * 100) + "%");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setOpaqueInsets(new Insets(10, 10, 10, 10));

        grid.add(persentage, 0, 0);
        grid.add(getErrorsViewPane(), 0, 1, 10, 10);

        Dialog dialog = new Dialog();
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
        dialog.showAndWait();
    }

    private enum QuizType {
        RANDOM,
        RATING,
        SESSION,
        ERRORS
    }

    private static class QuizProperties {
        private String sessionName;
        private int numberOfQuestions;
        private QuizType type;
        private boolean isQuestionsOnForeign;

        QuizProperties(QuizType type, int numberOfQuestions, boolean isQuestionsOnForeign) {
            this.type = type;
            this.numberOfQuestions = numberOfQuestions;
            this.isQuestionsOnForeign = isQuestionsOnForeign;
        }

        QuizProperties(QuizType type, int numberOfQuestions, boolean isQuestionsOnForeign,
                       String sessionName) {
            this.type = type;
            this.numberOfQuestions = numberOfQuestions;
            this.isQuestionsOnForeign = isQuestionsOnForeign;
            this.sessionName = sessionName;
        }

        public QuizType getQuizType() {
            return type;
        }

        public int getNumberOfQuestions() {
            return numberOfQuestions;
        }

        public String getSessionName() {
            return sessionName;
        }

        public boolean isQuestionsOnForeign() {
            return isQuestionsOnForeign;
        }
    }

    //костыли
    private int correctAnswers;
    private int currentIndex;
}
