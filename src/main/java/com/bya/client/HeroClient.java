package com.bya.client;

import com.bya.model.ClientRequest;
import com.bya.model.HeroAttackData;
import com.bya.model.ServerEvent;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class HeroClient {

    private static final String SERVER_IP = "172.25.144.1"; // <-- ИЗМЕНИТЬ НА IP СЕРВЕРA
    private static final int SERVER_PORT = 12345;
    private static int myHp = 100;
    private static volatile boolean isGameOver = false;

    public static void main(String[] args) {
        Gson gson = new Gson();

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String serverJson;
                    while ((serverJson = serverReader.readLine()) != null) {
                        ServerEvent event = gson.fromJson(serverJson, ServerEvent.class);
                        handleServerEvent(event);
                    }
                } catch (IOException e) {
                    if (!isGameOver) {
                        System.out.println("Сервер отключился.");
                    }
                }
            }).start();

            Scanner consoleScanner = new Scanner(System.in);
            System.out.println("Подключено к серверу. Введите 'attack' для атаки.");
            while (!isGameOver && consoleScanner.hasNextLine()) {
                String command = consoleScanner.nextLine();
                if ("attack".equalsIgnoreCase(command)) {
                    ClientRequest request = new ClientRequest();
                    request.action = "heroAttack";

                    HeroAttackData attackData = new HeroAttackData();
                    attackData.characterName = "Сэр Ланселот";
                    attackData.attackDamage = 20;
                    attackData.heroHp = myHp;
                    attackData.weaponType = "Меч";
                    request.data = attackData;

                    writer.println(gson.toJson(request));
                }
            }
            System.out.println("Игра окончена. Нажмите Enter для выхода.");

        } catch (IOException e) {
            System.out.println("Не удалось подключиться к серверу: " + e.getMessage());
        }
    }

    private static void handleServerEvent(ServerEvent event) {
        if (isGameOver) return;

        Map<String, Object> data = event.data;
        switch (event.eventType) {
            case "attackConfirmation":
                String message = (String) data.get("message");
                double villainHp = (Double) data.get("villainHp");
                System.out.println("Ответ сервера: " + message + " | Здоровье злодея: " + (int) villainHp);
                break;
            case "villainAttack":
                double damage = (Double) data.get("damageDealt");
                myHp -= (int) damage;
                System.out.println("Злодей атакует! Вы получили " + (int) damage + " урона. Ваше здоровье: " + myHp);
                if (myHp <= 0) {
                    System.out.println("Вы были повержены...");
                    // Здесь можно было бы отправить серверу сообщение о своем поражении
                }
                break;
            case "gameOver":
                isGameOver = true;
                String winner = (String) data.get("winner");
                System.out.println("==========================================");
                System.out.println("ИГРА ОКОНЧЕНА! Победитель: " + winner);
                System.out.println("==========================================");
                break;
        }
    }
}