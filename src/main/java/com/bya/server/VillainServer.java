package com.bya.server;

import com.bya.model.*;
import com.bya.server.game.GameState;
import com.bya.server.game.observer.GameObserver;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;

public class VillainServer {

    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        System.out.println("Сервер Злодея запущен.");
        printServerIp();

        GameObserver consoleLogger = createConsoleLogger();
        GameState.getInstance().addObserver(consoleLogger);

        startConsoleInput();

        ServerSocket serverSocket = new ServerSocket(12345);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            new ClientHandler(clientSocket);
        }
    }

    private static GameObserver createConsoleLogger() {
        return event -> {
            Object data = event.data;
            switch (event.eventType) {
                case "attackConfirmation":
                    if (data instanceof AttackConfirmationData) {
                        AttackConfirmationData confirmation = (AttackConfirmationData) data;
                        System.out.println("-> Герой атаковал. Здоровье злодея: " + confirmation.villainHp);
                    }
                    break;
                case "villainAttack":
                    if (data instanceof VillainAttackData) {
                        VillainAttackData attack = (VillainAttackData) data;
                        System.out.println("<- Злодей атакует! Урон: " + attack.damageDealt);
                    }
                    break;
                case "gameOver":
                    if (data instanceof GameOverData) {
                        GameOverData gameOver = (GameOverData) data;
                        System.out.println("!!! ИГРА ОКОНЧЕНА. Победитель: " + gameOver.winner + " !!!");
                    }
                    break;
            }
        };
    }

    private static void startConsoleInput() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите 'attack' чтобы атаковать героев.");
            while (scanner.hasNextLine()) {
                if ("attack".equalsIgnoreCase(scanner.nextLine())) {
                    GameState.getInstance().processVillainAttack();
                }
            }
        }).start();
    }

    private static class ClientHandler extends Thread implements GameObserver {
        private final Socket socket;
        private final PrintWriter writer;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            GameState.getInstance().addObserver(this);
            this.start();
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String json;
                while ((json = reader.readLine()) != null) {
                    try {
                        ClientRequest request = gson.fromJson(json, ClientRequest.class);
                        if ("heroAttack".equals(request.action)) {
                            GameState.getInstance().processHeroAttack(request.data);
                        }
                    } catch (JsonSyntaxException e) {
                        System.out.println("Получен неверный JSON.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Герой отключился.");
            } finally {
                GameState.getInstance().removeObserver(this);
            }
        }

        @Override
        public void update(ServerEvent event) {
            writer.println(gson.toJson(event));
        }
    }

    private static void printServerIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        System.out.println("==========================================");
                        System.out.println("  IP для подключения: " + addr.getHostAddress());
                        System.out.println("  Порт: 12345");
                        System.out.println("==========================================");
                        return;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}