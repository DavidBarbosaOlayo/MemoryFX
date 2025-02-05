package org.example.memoryfx;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class TableroCartasFX extends GridPane {

    private Button[][] botones;
    private MemoryClient client;
    private final int FILAS = 4;
    private final int COLUMNAS = 8;

    public TableroCartasFX() {
        botones = new Button[FILAS][COLUMNAS];
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(10));

        // Creamos los botones para cada carta
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Button btn = new Button("???");
                btn.setPrefSize(50, 50);
                final int fila = i;
                final int col = j;
                // Al pulsar un botón, se envía la jugada al servidor
                btn.setOnAction(e -> {
                    if (client != null) {
                        client.sendMove(fila, col);
                    }
                });
                botones[i][j] = btn;
                add(btn, j, i);  // En GridPane, el primer parámetro es la columna y el segundo la fila.
            }
        }
    }

    public void setClient(MemoryClient client) {
        this.client = client;
    }

    /**
     * Actualiza el tablero a partir de un mensaje recibido del servidor.
     * Se asume que el mensaje contiene varias líneas, cada una representando una fila,
     * con celdas separadas por espacios.
     *
     * Aquí se aplica un efecto de fade (desvanecimiento) a cada botón cuya
     * información cambie, lo que permite visualizar la transición (por ejemplo,
     * cuando se muestran las cartas volteadas y luego se ocultan).
     */
    public void updateFromServerMessage(String msg) {
        if (msg.contains("\n")) {
            String[] rows = msg.split("\n");
            for (int i = 0; i < rows.length && i < botones.length; i++) {
                // Separa las celdas; se usa "\\s+" para ignorar espacios múltiples.
                String[] cells = rows[i].trim().split("\\s+");
                for (int j = 0; j < cells.length && j < botones[i].length; j++) {
                    String newText = cells[j];
                    Button btn = botones[i][j];

                    // Si el texto actual es diferente, aplicamos una animación.
                    if (!btn.getText().equals(newText)) {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btn);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> {
                            btn.setText(newText);
                            btn.setDisable(!newText.equals("???"));

                            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), btn);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);
                            fadeIn.play();
                        });
                        fadeOut.play();
                    }
                }
            }
        } else {
            // Si el mensaje no es un tablero, se muestra en consola o se podría mostrar en un área de mensajes de la UI.
            System.out.println("Mensaje del servidor: " + msg);
        }
    }
}
