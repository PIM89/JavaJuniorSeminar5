package org.example;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 8181;
    private static long clientIdCounter = 1L;
    private static ConcurrentHashMap<Long, SocketWrapper> clients = new ConcurrentHashMap<>();
    private static String passwordAdmin = "admin";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                final Socket client = serverSocket.accept();
                final long clientId = clientIdCounter++;
                SocketWrapper wrapper = new SocketWrapper(clientId, client);
                System.out.println("Подключился новый клиент [id=" + clientId + "]");
                SendMessages.sendMessageAllByServer("Подключился новый клиент [id=" + clientId + "]");
                clients.put(clientId, wrapper);

                //запись
                Thread threadRec = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try (Scanner serverScanner = new Scanner(System.in)) {
                            String messageRule = "Ваш id=" + clientId + "\n" +
                                    "Для отправки конфиденциальных сообщений другому клиенту, например с id=777\n" +
                                    "используй форму: '@777 текст сообщения', где '777' - id клиента\n" +
                                    "Для авторизации в качестве администратора\n" +
                                    "используй форму: '@admin пароль'";

                            SendMessages.sendMessageByIdServer(clientId, "Соединение с сервером успешно!\n");
                            SendMessages.sendMessageByIdServer(clientId, getInfoByClients());
                            SendMessages.sendMessageByIdServer(clientId, messageRule);

                            while (true) {
                                String serverMessage = serverScanner.nextLine();
                                if (serverMessage.equals("q")) {
                                    System.out.println("Сервер отключится через 3 секунды!");
                                    SendMessages.sendMessageAllByServer("Сервер отключится через 3 секунды!");
                                    Thread.sleep(3000);
                                    serverSocket.close();
                                }
                                SendMessages.checkMsgAndSendByServer(clientId, serverMessage);
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                });
                threadRec.start();

                //чтение
                Thread threadRead = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try (Scanner input = wrapper.getInput()) {
                            while (true) {
                                String clientInput = input.nextLine();
                                System.out.println("Клиент с id=" + clientId + " пишет: " + clientInput);

                                if (Objects.equals("q", clientInput)) {
                                    clients.remove(clientId);
                                    SendMessages.sendMessageAll(clientId, "Клиент[id=" + clientId + "] отключился!");
                                }

                                if (!wrapper.isAdmin() && clientInput.startsWith("@admin")) {
                                    authorizationAdmin(wrapper, clientInput);
                                } else {
                                    SendMessages.checkMsgAndSend(clientId, clientInput);
                                }

                                if (wrapper.isAdmin() && clientInput.startsWith("kick")) {
                                    kickClient(wrapper, clientInput);
                                }
                            }
                        } catch (NoSuchElementException e) {
                            System.out.println("Клиент с id=" + clientId + " отключен от сервера");
                        }
                    }
                });
                threadRead.start();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при создании serverSocket!");
            e.printStackTrace();
        }
    }

    public static ConcurrentHashMap<Long, SocketWrapper> getClients() {
        return clients;
    }

    private static String getInfoByClients() {
        StringBuilder infoClients = new StringBuilder("Список всех клиентов:");
        for (Map.Entry<Long, SocketWrapper> entry : clients.entrySet()) {
            infoClients.append("\nКлиент с id=" + entry.getValue().getId());
        }
        return String.valueOf(infoClients.append("\n"));
    }

    private static void authorizationAdmin(SocketWrapper wrapper, String commandAndPassword) {
        String[] s = commandAndPassword.split(" ");
        if (s[1].equals(passwordAdmin)) {
            wrapper.setAdmin(true);
            System.out.println("Авторизовался в качестве администратора!");
            SendMessages.sendMessageByIdServer(wrapper.getId(), "Вы авторизовались как администратор!");
            SendMessages.sendMessageAll(wrapper.getId(), "Клиент с id=" + wrapper.getId() + " авторизовался в качестве администратора!");
        } else {
            System.out.println("Попытка клиента c id=" + wrapper.getId() + " отправить команду на сервер!");
            SendMessages.sendMessageByIdServer(wrapper.getId(), "Команда не найдена!");
        }
    }

    private static void kickClient(SocketWrapper wrapper, String commandKick) {
        String[] s = commandKick.split(" ");
        try {
            long l = Long.parseLong(s[1]);
            if (clients.containsKey(l)) {
                clients.values().stream().filter(it -> it.getId() == l).forEach(it -> {
                    try {
                        it.close();
                        clients.remove(it.getId());
                        SendMessages.checkMsgAndSendByServer(it.getId(), "Клиент с id=" + it.getId() + " отключился!");
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
            } else {
                SendMessages.sendMessageByIdServer(wrapper.getId(), "Клиента с id=" + l + " нет!");
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка при отключении клиента от сервера!");
            System.out.println(e.getMessage());
        }
    }
}