package org.example.memoryfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MemoryApp extends Application {

    private MemoryClient client;

    @Override
    public void start(Stage primaryStage) {
        Label headerLabel = new Label("Conectando al servidor...");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-padding: 10;");

        TableroCartasFX tablero = new TableroCartasFX();

        BorderPane root = new BorderPane();
        root.setTop(headerLabel);
        root.setCenter(tablero);

        Scene scene = new Scene(root, 1080, 720);
        primaryStage.setTitle("MemoryFX GAME");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Creamos y conectamos el cliente con el servidor (en este ejemplo localhost y puerto 12345)
        client = new MemoryClient("localhost", 12345, tablero, headerLabel);
        tablero.setClient(client);
        client.connect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
