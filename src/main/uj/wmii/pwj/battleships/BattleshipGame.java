package main.uj.wmii.pwj.battleships;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BattleshipGame {
    private String mode;
    private String host = "localhost";
    private int port;
    private String originalMapString;
    private char[][] startBoard = new char[10][10];
    private char[][] finalBoard = new char[10][10];
    private char[][] enemyBoard = new char[10][10];
    private String last;

    public static void main(String[] args) {
        BattleshipGame game = new BattleshipGame();
        game.parseArgs(args);
        game.setup();
        game.play();
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-mode")) mode = args[++i];
            else if (args[i].equals("-port")) port = Integer.parseInt(args[++i]);
            else if (args[i].equals("-host")) host = args[++i];
        }
    }

    private void setup() {
        originalMapString = BattleshipGenerator.defaultInstance().generateMap();
        for (int i = 0; i < 100; i++) {
            char c = originalMapString.charAt(i);
            startBoard[i / 10][i % 10] = c;
            finalBoard[i / 10][i % 10] = c;
            enemyBoard[i / 10][i % 10] = '?';
        }
        System.out.println("Mapa początkowa:");
        printBoard(startBoard);
        System.out.println();
    }

    public void play() {
        try (Socket s = "server".equalsIgnoreCase(mode) ? new ServerSocket(port).accept() : new Socket(host, port)) {
            s.setSoTimeout(1000);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);

            if ("client".equalsIgnoreCase(mode)) {
                send(out, "start;" + pick());
            }

            int errs = 0;
            while (true) {
                try {
                    String line = in.readLine();
                    if (line == null) break;
                    System.out.println(line);
                    errs = 0;

                    if (line.equals("ostatni zatopiony")) {
                        String enemyFullMap = in.readLine();
                        System.out.println("\nWYGRANA");
                        System.out.println("Mapa przeciwnika:");
                        printStringAsBoard(enemyFullMap);
                        System.out.println("\nMoja mapa:");
                        printBoard(finalBoard);
                        break;
                    }

                    String[] p = line.split(";");
                    update(p[0]);

                    if (p.length > 1) {
                        String resp = shot(p[1]);
                        if (resp.equals("ostatni zatopiony")) {
                            send(out, resp);
                            out.print(originalMapString + "\n");
                            out.flush();
                            System.out.println("\nPRZEGRANA");
                            System.out.println("Mapa przeciwnika:");
                            printBoard(enemyBoard);
                            System.out.println("\nMoja mapa:");
                            printBoard(finalBoard);
                            break;
                        }
                        send(out, resp + ";" + pick());
                    }
                } catch (SocketTimeoutException e) {
                    if (++errs >= 3) {
                        System.out.println("Błąd komunikacji");
                        System.exit(1);
                    }
                    send(out, last);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(PrintWriter out, String m) {
        last = m;
        out.print(m + "\n");
        out.flush();
        System.out.println(m);
    }

    private String shot(String c) {
        int r = c.charAt(0) - 'A', col = Integer.parseInt(c.substring(1)) - 1;
        if (startBoard[r][col] == '#' || startBoard[r][col] == 'X') {
            if (startBoard[r][col] == '#') {
                startBoard[r][col] = 'X';
                finalBoard[r][col] = '@';
            }
            if (isFleetSunk()) return "ostatni zatopiony";
            if (isShipSunk(r, col)) return "trafiony zatopiony";
            return "trafiony";
        } else {
            if (finalBoard[r][col] == '.') finalBoard[r][col] = '~';
            return "pudło";
        }
    }

    private void update(String cmd) {
        if (last == null || !last.contains(";")) return;
        String c = last.split(";")[1];
        int r = c.charAt(0) - 'A', col = Integer.parseInt(c.substring(1)) - 1;

        if (cmd.contains("trafiony")) {
            enemyBoard[r][col] = '#';
            if (cmd.equals("trafiony zatopiony")) {
                revealFullShip(r, col);
            }
        } else if (cmd.equals("pudło")) {
            if (enemyBoard[r][col] == '?') enemyBoard[r][col] = '.';
        }
    }

    private void revealFullShip(int r, int c) {
        List<int[]> shipParts = new ArrayList<>();
        findShipPartsOnEnemyBoard(r, c, new boolean[10][10], shipParts);
        for (int[] part : shipParts) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nr = part[0] + i, nc = part[1] + j;
                    if (nr >= 0 && nr < 10 && nc >= 0 && nc < 10 && enemyBoard[nr][nc] == '?') {
                        enemyBoard[nr][nc] = '.';
                    }
                }
            }
        }
    }

    private void findShipPartsOnEnemyBoard(int r, int c, boolean[][] visited, List<int[]> parts) {
        if (r < 0 || r > 9 || c < 0 || c > 9 || visited[r][c] || enemyBoard[r][c] != '#') return;
        visited[r][c] = true;
        parts.add(new int[]{r, c});
        findShipPartsOnEnemyBoard(r + 1, c, visited, parts);
        findShipPartsOnEnemyBoard(r - 1, c, visited, parts);
        findShipPartsOnEnemyBoard(r, c + 1, visited, parts);
        findShipPartsOnEnemyBoard(r, c - 1, visited, parts);
    }

    private boolean isShipSunk(int r, int c) {
        List<int[]> parts = new ArrayList<>();
        findShip(r, c, new boolean[10][10], parts);
        for (int[] p : parts) if (startBoard[p[0]][p[1]] == '#') return false;
        return true;
    }

    private void findShip(int r, int c, boolean[][] visited, List<int[]> parts) {
        if (r < 0 || r > 9 || c < 0 || c > 9 || visited[r][c] || (startBoard[r][c] != '#' && startBoard[r][c] != 'X')) return;
        visited[r][c] = true;
        parts.add(new int[]{r, c});
        findShip(r + 1, c, visited, parts);
        findShip(r - 1, c, visited, parts);
        findShip(r, c + 1, visited, parts);
        findShip(r, c - 1, visited, parts);
    }

    private boolean isFleetSunk() {
        for (char[] row : startBoard){
            for (char cell : row) if (cell == '#') return false;
        }
        return true;
    }

    private String pick() {
        Random r = new Random();
        return (char) ('A' + r.nextInt(10)) + "" + (r.nextInt(10) + 1);
    }

    private void printStringAsBoard(String map) {
        for (int i = 0; i < 100; i++) {
            System.out.print(map.charAt(i));
            if ((i + 1) % 10 == 0) System.out.println();
        }
    }

    private void printBoard(char[][] board) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) System.out.print(board[i][j]);
            System.out.println();
        }
    }
}