package com.bya.server;

import com.bya.model.ClientRequest;
import com.bya.model.ServerEvent;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class VillainServer {

    private static int villainHp = 100;
    private static boolean isGameOver = false;
    private static final List<PrintWriter> clientWriters = new ArrayList<>();
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        System.out.println("Сервер Злодея запущен.");
        printServerIp();
        startConsoleInput();

        ServerSocket serverSocket = new ServerSocket(12345);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            clientWriters.add(writer);
            new ClientHandler(clientSocket).start();
        }
    }

    private static void startConsoleInput() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите 'attack' чтобы атаковать героев.");
            while (scanner.hasNextLine()) {
                if (isGameOver) {
                    System.out.println("Игра окончена. Новые команды не принимаются.");
                    break;
                }
                String command = scanner.nextLine();
                if ("attack".equalsIgnoreCase(command)) {
                    int damage = 15;
                    System.out.println("Злодей атакует героев, нанося " + damage + " урона!");

                    ServerEvent villainAttackEvent = new ServerEvent();
                    villainAttackEvent.eventType = "villainAttack";

                    Map<String, Object> data = new HashMap<>();
                    data.put("damageDealt", damage);
                    data.put("villainHp", villainHp);
                    villainAttackEvent.data = data;

                    broadcast(villainAttackEvent);
                }
            }
        }).start();
    }

    private static void broadcast(ServerEvent event) {
        String jsonEvent = gson.toJson(event);
        for (PrintWriter writer : clientWriters) {
            writer.println(jsonEvent);
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String json;
                while ((json = reader.readLine()) != null) {
                    processClientRequest(json, this);
                }
            } catch (IOException e) {
                System.out.println("Герой отключился.");
            }
        }

        public void send(ServerEvent event) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println(gson.toJson(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void processClientRequest(String json, ClientHandler handler) {
        if (isGameOver) {
            return;
        }

        try {
            ClientRequest request = gson.fromJson(json, ClientRequest.class);
            if ("heroAttack".equals(request.action)) {
                villainHp -= request.data.attackDamage;
                System.out.println(request.data.characterName + " атаковал с помощью " + request.data.weaponType + ". Здоровье злодея: " + villainHp);

                ServerEvent response = new ServerEvent();
                response.eventType = "attackConfirmation";
                Map<String, Object> data = new HashMap<>();
                data.put("message", "Вы успешно атаковали злодея!");
                data.put("villainHp", villainHp < 0 ? 0 : villainHp);
                response.data = data;
                handler.send(response);

                if (villainHp <= 0) {
                    isGameOver = true;
                    System.out.println("Злодей повержен! Игра окончена.");
                    ServerEvent gameOverEvent = new ServerEvent();
                    gameOverEvent.eventType = "gameOver";
                    Map<String, Object> gameOverData = new HashMap<>();
                    gameOverData.put("winner", "Герои");
                    gameOverEvent.data = gameOverData;
                    broadcast(gameOverEvent);
                }
            }
        } catch (JsonSyntaxException e) {
            System.out.println("Получен неверный JSON: " + json);
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