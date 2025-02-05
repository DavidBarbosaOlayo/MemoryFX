package org.example.memoryfx;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MemoryServer {

    private final int port;
    private final GameLogic gameLogic;
    private final List<ClientHandler> clients;
    private int turnoActual = 0; // 0 = Jugador 1, 1 = Jugador 2
    private boolean juegoTerminado = false;

    public MemoryServer(int port, GameLogic gameLogic) {
        this.port = port;
        this.gameLogic = gameLogic;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado en el puerto " + port);

            // Esperamos a que se conecten 2 clientes
            while (clients.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, clients.size());
                clients.add(handler);
                new Thread(handler).start();
                System.out.println("Jugador conectado: " + handler.getPlayerId());
            }

            // Cuando están conectados ambos jugadores, enviamos mensaje inicial
            broadcast("¡Juego iniciado! Turno del Jugador 1");
            broadcastTablero();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Envía un mensaje a todos los clientes
    private void broadcast(String message) {
        for (ClientHandler handler : clients) {
            handler.sendMessage(message);
        }
    }

    private void broadcastTablero() {
        String boardStr = getBoardString().replace("\n", "#");
        broadcast(boardStr);
        System.out.println("--- Tablero actualizado ---");
        System.out.println(boardStr);
    }


    // Genera una representación textual del tablero
    private String getBoardString() {
        StringBuilder sb = new StringBuilder();
        String[][] tablero = gameLogic.getTablero();
        boolean[][] revelado = gameLogic.getRevelado();
        for (int i = 0; i < tablero.length; i++) {
            for (int j = 0; j < tablero[i].length; j++) {
                if (revelado[i][j]) {
                    sb.append(tablero[i][j]);
                } else {
                    sb.append("???");
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // Clase interna que maneja a cada cliente conectado
    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private final PrintWriter out;
        private final BufferedReader in;

        // Variables para llevar la cuenta de la jugada actual
        private int jugadaActual = 0; // 0 = esperando primera jugada, 1 = esperando segunda
        private int filaPrimera = -1, colPrimera = -1;

        public ClientHandler(Socket socket, int playerId) throws IOException {
            this.socket = socket;
            this.playerId = playerId;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public int getPlayerId() {
            return playerId;
        }

        public void sendMessage(String msg) {
            out.println(msg);
        }

        @Override
        public void run() {
            sendMessage("Bienvenido, eres el Jugador " + (playerId + 1));

            try {
                String inputLine;
                while (!juegoTerminado && (inputLine = in.readLine()) != null) {
                    // Esperamos mensajes con formato "MOVE fila columna"
                    if (!inputLine.startsWith("MOVE")) continue;

                    // Si no es el turno de este jugador, se ignoran los movimientos
                    if (playerId != turnoActual) {
                        sendMessage("No es tu turno.");
                        continue;
                    }

                    String[] parts = inputLine.split(" ");
                    if (parts.length < 3) continue;
                    int fila = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);

                    if (jugadaActual == 0) {
                        // Primera jugada: se intenta voltear la carta
                        if (!gameLogic.voltearCarta(fila, col)) {
                            sendMessage("Movimiento inválido o carta ya revelada. Intenta otra vez.");
                            continue;
                        }
                        filaPrimera = fila;
                        colPrimera = col;
                        jugadaActual = 1;
                        broadcastTablero();
                        sendMessage("Esperando tu segunda jugada.");
                    } else {
                        // Segunda jugada: se voltea la carta y se comprueba si hay pareja
                        if (!gameLogic.voltearCarta(fila, col)) {
                            sendMessage("Movimiento inválido o carta ya revelada. Intenta otra vez.");
                            continue;
                        }
                        broadcastTablero();

                        // Comprobamos la pareja
                        if (gameLogic.comprobarSiCoinciden(filaPrimera, colPrimera, fila, col)) {
                            broadcast("¡Jugador " + (playerId + 1) + " encontró una pareja!");
                        } else {
                            // Si no coincide, esperamos 1 segundo y ocultamos las cartas
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            gameLogic.esconderCartas(filaPrimera, colPrimera, fila, col);
                            broadcastTablero();
                            // Cambiamos turno
                            turnoActual = (turnoActual + 1) % 2;
                            broadcast("Turno del Jugador " + (turnoActual + 1));
                        }
                        // Reiniciamos la jugada para este turno
                        jugadaActual = 0;
                        filaPrimera = -1;
                        colPrimera = -1;
                    }

                    if (gameLogic.verificarJuegoTerminado()) {
                        broadcast("¡Juego terminado!");
                        juegoTerminado = true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
    }

    public static void main(String[] args) {
        int filas = 5, columnas = 8;
        GameLogic gameLogic = new GameLogic(filas, columnas);
        MemoryServer server = new MemoryServer(12345, gameLogic);
        server.start();
    }
}

