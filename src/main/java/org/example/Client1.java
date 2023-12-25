package org.example;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;


public class Client1 {
    private final static String HOST = "localhost";
    private final static int PORT = 8181;

    public static void main(String[] args) {
        try {
            Socket client = new Socket(HOST, PORT);
            Thread treadRead = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (Scanner input = new Scanner(client.getInputStream())) {
                        while (true) {
                             String inputString = input.nextLine();
                            System.out.println(inputString);
                        }
                    } catch (IOException | NoSuchElementException e) {
                        System.out.println("Вы отключились от сервера!");
                    }
                }
            });
            treadRead.start();

            Thread threadRec = new Thread(new Runnable() {
                @Override
                public void run() {
                    try (PrintWriter output = new PrintWriter(client.getOutputStream(), true)) {
                        Scanner consoleScanner = new Scanner(System.in);
                        while (true) {
                            String consoleInput = consoleScanner.nextLine();
                            output.println(consoleInput);
                            if (Objects.equals("q", consoleInput)) {
                                client.close();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            });

            threadRec.start();
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}


