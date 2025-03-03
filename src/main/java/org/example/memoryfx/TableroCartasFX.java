package org.example.memoryfx;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TableroCartasFX extends GridPane {

    private Button[][] botones;
    private MemoryClient client;
    private final int FILAS = 4;
    private final int COLUMNAS = 8;

    // Caché de imágenes
    private final Map<String, Image> imageCache = new HashMap<>();

    public TableroCartasFX() {
        botones = new Button[FILAS][COLUMNAS];
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(10));

        // Configurar restricciones para distribuir equitativamente las celdas
        for (int j = 0; j < COLUMNAS; j++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / COLUMNAS);
            getColumnConstraints().add(cc);
        }
        for (int i = 0; i < FILAS; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / FILAS);
            getRowConstraints().add(rc);
        }

        // Cargar la caché de imágenes (suponiendo que tus imágenes se llaman card1.png, card2.png, ... card16.png)
        cargarImagenes();

        // Crear los botones (cartas)
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Button btn = new Button("???");
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btn.getStyleClass().add("card");
                // Inicia como carta oculta (dorso)
                btn.getStyleClass().add("card-back");

                final int fila = i;
                final int col = j;
                btn.setOnAction(e -> {
                    if (client != null) {
                        client.sendMove(fila, col);
                    }
                });

                botones[i][j] = btn;
                add(btn, j, i);
            }
        }
    }

    // Método para cargar imágenes en la caché
    private void cargarImagenes() {
        // Aquí asumimos que las imágenes se llaman card1.png hasta card16.png
        for (int i = 1; i <= 16; i++) {
            String fileName = "card" + i + ".png";
            InputStream is = getClass().getResourceAsStream("/assets/cartas/" + fileName);
            if (is != null) {
                Image img = new Image(is);
                imageCache.put(fileName, img);
            } else {
                System.err.println("No se encontró la imagen: " + fileName);
            }
        }
    }

    public void setClient(MemoryClient client) {
        this.client = client;
    }

    /**
     * Actualiza el tablero a partir de un mensaje recibido del servidor.
     * Se espera que el mensaje contenga varias líneas, cada una representando una fila,
     * con celdas separadas por espacios.
     * Cuando una carta está oculta se muestra "???"; cuando está descubierta se muestra la imagen
     * cuyo nombre se encuentra en el valor de la casilla (por ejemplo, "card3.png").
     */
    public void updateFromServerMessage(String msg) {
        if (msg.contains("\n")) {
            String[] rows = msg.split("\n");
            for (int i = 0; i < rows.length && i < botones.length; i++) {
                String[] cells = rows[i].trim().split("\\s+");
                for (int j = 0; j < cells.length && j < botones[i].length; j++) {
                    String newValue = cells[j];
                    Button btn = botones[i][j];

                    // Si la carta ya está en el estado correcto, no se anima:
                    if (newValue.equals("???") && btn.getStyleClass().contains("card-back")) {
                        continue;
                    }
                    if (!newValue.equals("???") && btn.getStyleClass().contains("card-face")) {
                        continue;
                    }

                    // Solo actualizamos si el valor ha cambiado (para no animar de nuevo una carta revelada)
                    if (!btn.getText().equals(newValue)) {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btn);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> {
                            if (newValue.equals("???")) {
                                // Carta oculta: mostramos reverso
                                btn.setText("???");
                                btn.setGraphic(null);
                                btn.getStyleClass().removeAll("card-face");
                                if (!btn.getStyleClass().contains("card-back")) {
                                    btn.getStyleClass().add("card-back");
                                }
                                btn.setDisable(false);
                            } else {
                                // Carta descubierta: removemos el estilo de reverso y cargamos la imagen desde la caché
                                btn.getStyleClass().remove("card-back");
                                if (!btn.getStyleClass().contains("card-face")) {
                                    btn.getStyleClass().add("card-face");
                                }
                                Image img = imageCache.get(newValue);
                                if (img == null) {
                                    System.err.println("La imagen no se cargó correctamente para: " + newValue);
                                    return;
                                }
                                ImageView iv = new ImageView(img);
                                iv.setFitWidth(btn.getWidth() - 50);
                                iv.setFitHeight(btn.getHeight() - 50);
                                iv.setPreserveRatio(true);
                                btn.setGraphic(iv);
                                btn.setText("");
                                btn.setDisable(true);
                            }
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
            System.out.println("Mensaje del servidor: " + msg);
        }
    }
}
