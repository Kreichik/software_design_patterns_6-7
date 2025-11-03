package com.bya.client;

import com.bya.model.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class HeroClient {

    private static final String SERVER_IP = "172.25.144.1"; // <-- ИЗМЕНИТЬ НА IP СЕРВЕРА
    private static final int SERVER_PORT = 12345;
    private static int myHp = 100;
    private static volatile boolean isGameOver = false;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String serverJson;
                    while ((serverJson = serverReader.readLine()) != null) {
                        handleServerEvent(serverJson);
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

    private static void handleServerEvent(String serverJson) {
        if (isGameOver) return;

        ServerEvent event = gson.fromJson(serverJson, ServerEvent.class);
        String eventDataJson = gson.toJson(event.data);

        switch (event.eventType) {
            case "attackConfirmation":
                AttackConfirmationData confirmData = gson.fromJson(eventDataJson, AttackConfirmationData.class);
                System.out.println("Ответ сервера: " + confirmData.message + " | Здоровье злодея: " + confirmData.villainHp);
                break;

            case "villainAttack":
                VillainAttackData attackData = gson.fromJson(eventDataJson, VillainAttackData.class);
                myHp -= attackData.damageDealt;
                System.out.println("Злодей атакует! Вы получили " + attackData.damageDealt + " урона. Ваше здоровье: " + myHp);
                if (myHp <= 0) {
                    System.out.println("Вы были повержены...");
                }
                break;

            case "gameOver":
                isGameOver = true;
                GameOverData gameOverData = gson.fromJson(eventDataJson, GameOverData.class);
                System.out.println("==========================================");
                System.out.println("ИГРА ОКОНЧЕНА! Победитель: " + gameOverData.winner);
                System.out.println("==========================================");
                break;
        }
    }
}