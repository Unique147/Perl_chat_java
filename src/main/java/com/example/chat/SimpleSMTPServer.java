package com.example.chat;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


import javax.mail.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class SimpleSMTPServer {

    public static void main(String[] args) {
        int portNumber = 25; // Порт SMTP сервера

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("SMTP Server started on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection: " + clientSocket.getInetAddress().getHostAddress());
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) {
            String inputLine;
            out.println("220 localhost SimpleSMTPServer ready");

            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client: " + inputLine);

                // Обработка команды MAIL FROM
                if (inputLine.startsWith("MAIL FROM")) {
                    out.println("250 OK");
                }
                // Обработка команды RCPT TO
                else if (inputLine.startsWith("RCPT TO")) {
                    out.println("250 OK");
                }
                // Обработка команды DATA
                else if (inputLine.equals("DATA")) {
                    out.println("354 Start mail input; end with <CRLF>.<CRLF>");
                    // Прочитать тело письма
                    StringBuilder message = new StringBuilder();
                    while (!(inputLine = in.readLine()).equals(".")) {
                        message.append(inputLine).append("\r\n");
                    }
                    System.out.println("Received message:");
                    System.out.println(message.toString());

                    // Извлечь адрес электронной почты из сообщения
                    String recipientEmail = extractEmail(message.toString());
                    if (recipientEmail != null) {
                        // Отправить код подтверждения
                        String verificationCode = VerificationCodeGenerator.generateVerificationCode();
                        sendVerificationCode(recipientEmail, verificationCode);
                        System.out.println("Код подтверждения отправлен на " + recipientEmail);
                        out.println("250 OK");
                    } else {
                        out.println("550 Failed to extract recipient email");
                    }
                }
                // Обработка команды QUIT
                else if (inputLine.equals("QUIT")) {
                    out.println("221 Bye");
                    break;
                }
                // Неизвестная команда
                else {
                    out.println("500 Syntax error, command unrecognized");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String extractEmail(String message) {
        // Реализация извлечения адреса электронной почты из тела сообщения
        // Вам нужно будет написать соответствующий код
        return "recipient@example.com"; // Вернуть тестовый адрес
    }

    private static void sendVerificationCode(String recipientEmail, String verificationCode) {
        // Настройки подключения к SMTP серверу
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.example.com"); // Замените на адрес вашего SMTP сервера
        props.put("mail.smtp.port", "25"); // Укажите порт вашего SMTP сервера

        // Создание сессии для отправки сообщения
        Session session = Session.getInstance(props);

        try {
            // Создание сообщения
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@example.com")); // Замените на ваш адрес отправителя
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Код подтверждения для регистрации");
            message.setText("Ваш код подтверждения для регистрации: " + verificationCode);

            // Отправка сообщения
            Transport.send(message);

            System.out.println("Код подтверждения отправлен на " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
