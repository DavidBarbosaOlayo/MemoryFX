package org.example.memoryfx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.io.*;
import java.net.Socket;

public class MemoryClient {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final TableroCartasFX tablero;
    private final Label headerLabel;
    private final Label scoreLabel;
    private final Label identityLabel;
    private Thread listenerThread;

    public MemoryClient(String host, int port, TableroCartasFX tablero, Label headerLabel, Label scoreLabel, Label identityLabel) {
        this.host = host;
        this.port = port;
        this.tablero = tablero;
        this.headerLabel = headerLabel;
        this.scoreLabel = scoreLabel;
        this.identityLabel = identityLabel;
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            startListener();
        } catch (IOException e) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error al conectar con el servidor", ButtonType.OK);
                alert.showAndWait();
            });
        }
    }

    public void sendMove(int fila, int col) {
        if (out != null) {
            out.println("MOVE " + fila + " " + col);
        }
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    Platform.runLater(() -> {
                        if (msg.startsWith("Bienvenido,")) {
                            identityLabel.setText(msg);
                        } else if (msg.startsWith("SCORE:")) {
                            scoreLabel.setText(msg.substring("SCORE:".length()).trim());
                        } else if (msg.contains("#")) {
                            String boardStr = msg.replace("#", "\n");
                            tablero.updateFromServerMessage(boardStr);
                        } else {
                            headerLabel.setText(msg);
                            if (msg.startsWith("Ganador:") || msg.startsWith("Empate")) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
                                alert.setTitle("Fin del Juego");
                                alert.setHeaderText("Resultado Final");
                                alert.showAndWait();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
}
