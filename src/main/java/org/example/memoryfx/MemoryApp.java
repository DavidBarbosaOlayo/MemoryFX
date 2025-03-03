package org.example.memoryfx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MemoryApp extends Application {

    private MemoryClient client;

    @Override
    public void start(Stage primaryStage) {
        // Label para mensajes generales (estado del juego)
        Label headerLabel = new Label("Conectando al servidor...");

        // Label para mostrar la identidad del jugador
        Label identityLabel = new Label("Identidad: -");

        // Label para el marcador
        Label scoreLabel = new Label("Jugador 1: 0 | Jugador 2: 0");
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-padding: 10;");

        // Agrupamos los labels en un VBox centrado
        VBox topBox = new VBox(headerLabel, identityLabel, scoreLabel);
        topBox.setAlignment(Pos.CENTER);

        TableroCartasFX tablero = new TableroCartasFX();

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(tablero);

        Scene scene = new Scene(root, 1080, 925);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("MemoryFX GAME");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Se actualiza el cliente para que reciba el nuevo label de identidad
        client = new MemoryClient("localhost", 12345, tablero, headerLabel, scoreLabel, identityLabel);
        tablero.setClient(client);
        client.connect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
