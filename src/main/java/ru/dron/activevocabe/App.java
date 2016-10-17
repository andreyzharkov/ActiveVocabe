package ru.dron.activevocabe;

import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import ru.dron.activevocabe.controllers.RootPaneController;
import ru.dron.activevocabe.model.SharedData;

public class App extends Application {
    private SharedData sharedData = SharedData.getSharedData();

    @Override
    public void start(Stage primaryStage) {
        sharedData.setRootStage(primaryStage);

        try {
            HBox root = FXMLLoader.load(getClass().getResource("/fxml/RootPane.fxml"));

            Scene scene = new Scene(root, 800, 600);
            String cssPath = "/css/styles.css";
            scene.getStylesheets().add(cssPath);

            primaryStage.setTitle("Active Vocabe");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1500);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
