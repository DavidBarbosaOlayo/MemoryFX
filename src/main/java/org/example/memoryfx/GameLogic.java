package org.example.memoryfx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameLogic {
    private final int filas;
    private final int columnas;
    private String[][] tablero;
    private boolean[][] revelado;

    public GameLogic(int filas, int columnas) {
        if ((filas * columnas) % 2 != 0) {
            throw new IllegalArgumentException("El tablero debe tener un número par de casillas.");
        }
        this.filas = filas;
        this.columnas = columnas;
        inicializarTablero();
    }

    private void inicializarTablero() {
        tablero = new String[filas][columnas];
        revelado = new boolean[filas][columnas];

        // Crear parejas de caracteres ASCII
        List<String> cartas = new ArrayList<>();
        int parejasTotales = (filas * columnas) / 2;
        char caracter = 33; // Caracter ASCII inicial ('!')
        for (int i = 0; i < parejasTotales; i++) {
            cartas.add(String.valueOf(caracter));
            cartas.add(String.valueOf( caracter));
            caracter++;
        }

        // Mezclamos las cartas
        Collections.shuffle(cartas);

        // Llenamos el tablero con las cartas
        int indice = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                tablero[i][j] = cartas.get(indice++);
                revelado[i][j] = false; // inicialmente están boca abajo todas
            }
        }
    }

    public boolean voltearCarta(int fila, int columna) {
        if (fila < 0 || fila >= filas || columna < 0 || columna >= columnas) {
            return false;
        }
        if (revelado[fila][columna]) {
            return false;
        }
        revelado[fila][columna] = true;
        return true;
    }

    public boolean comprobarSiCoinciden(int fila1, int col1, int fila2, int col2) {
        if (!revelado[fila1][col1] || !revelado[fila2][col2]) {
            return false;
        }
        return tablero[fila1][col1].equals(tablero[fila2][col2]);
    }

    public void esconderCartas(int fila1, int col1, int fila2, int col2) {
        revelado[fila1][col1] = false;
        revelado[fila2][col2] = false;
    }

    public boolean verificarJuegoTerminado() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (!revelado[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public String[][] getTablero() {
        return tablero;
    }

    public boolean[][] getRevelado() {
        return revelado;
    }
}


