package ru.dron.activevocabe;

import javafx.fxml.FXMLLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.dron.activevocabe.controls.PTableView;
import ru.dron.activevocabe.controls.PTreeView;
import ru.dron.activevocabe.model.SharedData;

public class App extends Application {
    private SharedData sharedData = SharedData.getSharedData();

    @Override
    public void start(Stage primaryStage) {
        sharedData.setRootStage(primaryStage);

        try {
            AnchorPane root = FXMLLoader.load(getClass().getResource("/fxml/RootPane.fxml"));

            Scene scene = new Scene(root, 800, 600);
            String cssPath = "/css/styles.css";
            scene.getStylesheets().add(cssPath);

            scene.widthProperty().addListener((a, b, c) -> {
                ((PTableView)root.lookup("#tableView")).resize();
                ((PTreeView)root.lookup("#treeView")).resize();
            });
            scene.heightProperty().addListener((a, b, c) -> {
                ((PTableView)root.lookup("#tableView")).resize();
                ((PTreeView)root.lookup("#treeView")).resize();
            });
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
