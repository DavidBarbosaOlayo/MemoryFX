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

public class TableroCartasFX extends GridPane {

    private Button[][] botones;
    private MemoryClient client;
    private final int filas;
    private final int columnas;
    private final Map<String, Image> imageCache = new HashMap<>();

    public TableroCartasFX(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
        botones = new Button[filas][columnas];
        setHgap(5);
        setVgap(5);
        setPadding(new Insets(10));

        // Configurar restricciones para distribuir equitativamente las celdas
        for (int j = 0; j < columnas; j++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / columnas);
            getColumnConstraints().add(cc);
        }
        for (int i = 0; i < filas; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / filas);
            getRowConstraints().add(rc);
        }

        cargarImagenes();

        // Crear los botones (cartas)
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                Button btn = new Button("???");
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btn.getStyleClass().add("card");
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

    private void cargarImagenes() {
        int parejasTotales = (filas * columnas) / 2;
        for (int i = 1; i <= parejasTotales; i++) {
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

    public void updateFromServerMessage(String msg) {
        if (msg.contains("\n")) {
            String[] rows = msg.split("\n");
            for (int i = 0; i < rows.length && i < botones.length; i++) {
                String[] cells = rows[i].trim().split("\\s+");
                for (int j = 0; j < cells.length && j < botones[i].length; j++) {
                    String newValue = cells[j];
                    Button btn = botones[i][j];

                    // Si el botón ya tiene el estilo correcto, se omite la animación
                    if (newValue.equals("???") && btn.getStyleClass().contains("card-back")) continue;
                    if (!newValue.equals("???") && btn.getStyleClass().contains("card-face")) continue;

                    if (!btn.getText().equals(newValue)) {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), btn);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> {
                            if (newValue.equals("???")) {
                                btn.setText("???");
                                btn.setGraphic(null);
                                btn.getStyleClass().removeAll("card-face");
                                if (!btn.getStyleClass().contains("card-back")) {
                                    btn.getStyleClass().add("card-back");
                                }
                                btn.setDisable(false);
                            } else {
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
