package org.example;


public class SendMessages {

    public static void sendMessageAll(Long id, String message) {
        Server.getClients().values().stream().filter(it -> it.getId() != id)
                .forEach(it -> it.getOutput().println("Клиент с id=" + id + " пишет: " + message));
    }

    public static void sendMessageAllByServer(String message) {
        Server.getClients().values().forEach(it -> it.getOutput().println("Сообщение от сервера: " + message));
    }

    public static void sendMessageById(Long id, String message) {
        if (!Server.getClients().values().equals(id)) {
            Server.getClients().values().stream().filter(it -> it.getId() == id).forEach(it -> it.getOutput().println("Клиент с id=" + id + " пишет: " + message));
        } else {
            System.out.println("Клиента с таким id нет!");
        }
    }

    public static void sendMessageByIdServer(Long id, String message) {
        if (!Server.getClients().values().equals(id)) {
            Server.getClients().values().stream().filter(it -> it.getId() == id).forEach(it -> it.getOutput().println("Сообщение с сервера: " + message));
        } else {
            System.out.println("Клиента с таким id нет!");
        }
    }

    public static void checkMsgAndSend(Long id, String msg) {
        char[] chars = msg.toCharArray();
        if (!(chars[0] == '@')) {
            sendMessageAll(id, msg);
        } else {
            int k = 0;
            for (int i = 1; i < chars.length; i++) {
                if (chars[i] == ' ') {
                    k = i;
                    break;
                }
            }
            try {
                long destId = Long.parseLong(msg.substring(1, k));
                if (Server.getClients().containsKey(id)) {
                    sendMessageById(destId, msg);
                } else {
                    SendMessages.sendMessageByIdServer(id, "Клиента с id=" + id + " нет! Сообщение не отправлено!");
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                SendMessages.sendMessageByIdServer(id, "Нарушена форма сообщения! Сообщение не отправлено!");
                System.out.println(e.getMessage() + "\n");
            }
        }
    }

    public static void checkMsgAndSendByServer(Long id, String msg) {
        char[] chars = msg.toCharArray();
        if (!(chars[0] == '@')) {
            sendMessageAllByServer(msg);
        } else {
            int k = 0;
            for (int i = 1; i < chars.length; i++) {
                if (chars[i] == ' ') {
                    k = i;
                    break;
                }
            }
            try {
                long destId = Long.parseLong(msg.substring(1, k));
                if (Server.getClients().containsKey(id)) {
                    sendMessageByIdServer(destId, msg);
                } else {
                    SendMessages.sendMessageByIdServer(id, "Клиента с id=" + id + " нет! Сообщение не отправлено!");
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                SendMessages.sendMessageByIdServer(id, "Нарушена форма сообщения! Сообщение не отправлено!");
                System.out.println(e.getMessage() + "\n");
            }
        }
    }
}
