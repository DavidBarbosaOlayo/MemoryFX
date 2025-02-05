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
    private Thread listenerThread;

    public MemoryClient(String host, int port, TableroCartasFX tablero, Label headerLabel) {
        this.host = host;
        this.port = port;
        this.tablero = tablero;
        this.headerLabel = headerLabel;
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
                        // Si el mensaje contiene el delimitador '#' asumimos que es la representación del tablero
                        if (msg.contains("#")) {
                            // Convertimos el '#' de vuelta a saltos de línea
                            String boardStr = msg.replace("#", "\n");
                            tablero.updateFromServerMessage(boardStr);
                        } else {
                            headerLabel.setText(msg);
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
