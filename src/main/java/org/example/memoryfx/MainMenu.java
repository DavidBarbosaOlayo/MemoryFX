package org.example.memoryfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu extends Application {

    private Stage primaryStage;
    private Scene menuScene;
    private Scene optionsScene;

    // Valor por defecto: 4x8 para mantener el tablero original
    private String boardSize = "4x8";

    private ComboBox<String> boardSizeComboBox;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Memory FX - Menú Principal");
        showMenu();
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> Platform.exit());

    }

    private void showMenu() {
        // Dos botones para definir el rol: quien crea la partida (inicia el servidor) y quien se une (solo cliente)
        Button createGameButton = new Button("Crear partida");
        Button joinGameButton = new Button("Unirse a partida");
        Button optionsButton = new Button("Opciones");
        Button exitButton = new Button("Salir");

        createGameButton.setOnAction(e -> startGame(true));
        joinGameButton.setOnAction(e -> startGame(false));
        optionsButton.setOnAction(e -> showOptions());
        exitButton.setOnAction(e -> Platform.exit());

        // Aplicamos la clase de estilo "menu-button" a cada botón para personalizarlos
        Button[] buttons = { createGameButton, joinGameButton, optionsButton, exitButton };
        for (Button b : buttons) {
            b.getStyleClass().add("menu-button");
        }

        VBox menuBox = new VBox(15, createGameButton, joinGameButton, optionsButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);
        menuScene = new Scene(menuBox, 400, 300);
        menuScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setScene(menuScene);

        // Igualamos el ancho de todos los botones al del más largo
        Platform.runLater(() -> {
            double maxWidth = 0;
            for (Button b : buttons) {
                b.applyCss();
                b.layout();
                maxWidth = Math.max(maxWidth, b.getWidth());
            }
            for (Button b : buttons) {
                b.setMinWidth(maxWidth);
            }
        });
    }

    private void showOptions() {
        Label label = new Label("Selecciona el tamaño del tablero:");
        boardSizeComboBox = new ComboBox<>();
        boardSizeComboBox.getItems().addAll("4x8", "5x6", "4x5", "3x4");
        boardSizeComboBox.setValue(boardSize);

        Button backButton = new Button("Volver");
        backButton.getStyleClass().add("menu-button");
        backButton.setOnAction(e -> {
            boardSize = boardSizeComboBox.getValue();
            primaryStage.setScene(menuScene);
        });

        VBox optionsBox = new VBox(15, label, boardSizeComboBox, backButton);
        optionsBox.setAlignment(Pos.CENTER);
        optionsScene = new Scene(optionsBox, 400, 300);
        optionsScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setScene(optionsScene);
    }

    /**
     * @param isCreator Si es true, esta instancia crea la partida (inicia el servidor)
     *                  Si es false, solo se conecta como cliente.
     */
    private void startGame(boolean isCreator) {
        // Parseamos el tamaño del tablero (formato "filas x columnas")
        String[] parts = boardSize.split("x");
        int filas = Integer.parseInt(parts[0]);
        int columnas = Integer.parseInt(parts[1]);

        // Creamos la lógica del juego y el tablero (la interfaz se crea igual que en MemoryApp)
        GameLogic gameLogic = new GameLogic(filas, columnas);
        TableroCartasFX tablero = new TableroCartasFX(filas, columnas);

        // Creamos la zona superior con los labels para el estado del juego
        Label headerLabel = new Label("Conectando al servidor...");
        Label identityLabel = new Label("Identidad: -");
        Label scoreLabel = new Label("Jugador 1: 0 | Jugador 2: 0");
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-padding: 10;");
        VBox topBox = new VBox(headerLabel, identityLabel, scoreLabel);
        topBox.setAlignment(Pos.CENTER);

        BorderPane gameRoot = new BorderPane();
        gameRoot.setTop(topBox);
        gameRoot.setCenter(tablero);

        Scene gameScene = new Scene(gameRoot, 1080, 925);
        gameScene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setScene(gameScene);

        // Solo el creador inicia el servidor
        if (isCreator) {
            MemoryServer server = new MemoryServer(12345, gameLogic);
            new Thread(server::start).start();
        }

        // Se inicializa el cliente (todos se conectan igual)
        MemoryClient client = new MemoryClient("localhost", 12345, tablero, headerLabel, scoreLabel, identityLabel);
        tablero.setClient(client);
        client.connect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
