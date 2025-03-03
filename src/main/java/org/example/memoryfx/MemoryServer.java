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
    private int scorePlayer1 = 0;
    private int scorePlayer2 = 0;

    public MemoryServer(int port, GameLogic gameLogic) {
        this.port = port;
        this.gameLogic = gameLogic;
        this.clients = new ArrayList<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado en el puerto " + port);

            // Esperar a que se conecten 2 clientes
            while (clients.size() < 2) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, clients.size());
                clients.add(handler);
                new Thread(handler).start();
                System.out.println("Jugador conectado: " + handler.getPlayerId());
            }

            // Enviar mensaje inicial y tablero
            broadcast("¡Juego iniciado! Turno del Jugador 1");
            broadcastTablero();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final int playerId;
        private final PrintWriter out;
        private final BufferedReader in;
        private int jugadaActual = 0;
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
                    if (!inputLine.startsWith("MOVE")) continue;

                    if (playerId != turnoActual) {
                        sendMessage("No es tu turno.");
                        continue;
                    }

                    String[] parts = inputLine.split(" ");
                    if (parts.length < 3) continue;
                    int fila = Integer.parseInt(parts[1]);
                    int col = Integer.parseInt(parts[2]);

                    if (jugadaActual == 0) {
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
                        if (!gameLogic.voltearCarta(fila, col)) {
                            sendMessage("Movimiento inválido o carta ya revelada. Intenta otra vez.");
                            continue;
                        }
                        broadcastTablero();

                        if (gameLogic.comprobarSiCoinciden(filaPrimera, colPrimera, fila, col)) {
                            if (playerId == 0) {
                                scorePlayer1++;
                            } else {
                                scorePlayer2++;
                            }
                            broadcast("¡Jugador " + (playerId + 1) + " encontró una pareja!");
                            broadcastScore();
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            gameLogic.esconderCartas(filaPrimera, colPrimera, fila, col);
                            broadcastTablero();
                            turnoActual = (turnoActual + 1) % 2;
                            broadcast("Turno del Jugador " + (turnoActual + 1));
                        }
                        jugadaActual = 0;
                        filaPrimera = -1;
                        colPrimera = -1;
                    }

                    if (gameLogic.verificarJuegoTerminado()) {
                        broadcast("¡Juego terminado!");
                        String resultado;
                        if (scorePlayer1 > scorePlayer2) {
                            resultado = "Ganador: Jugador 1";
                        } else if (scorePlayer2 > scorePlayer1) {
                            resultado = "Ganador: Jugador 2";
                        } else {
                            resultado = "Empate";
                        }
                        String mensajeFinal = resultado + "\nMarcador final: Jugador 1: " + scorePlayer1 + " | Jugador 2: " + scorePlayer2;
                        broadcast(mensajeFinal);
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

    private void broadcastScore() {
        String scoreMsg = "SCORE: Jugador 1: " + scorePlayer1 + " | Jugador 2: " + scorePlayer2;
        broadcast(scoreMsg);
    }
}
