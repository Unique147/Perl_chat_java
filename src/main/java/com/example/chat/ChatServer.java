package com.example.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer {

    private static final int PORT = 12345;
    private static final Map<String, ClientHandler> clients = new HashMap<>();
    private static final Map<String, String> confirmationCodes = new HashMap<>();
    private static final int nextUniqueCode = 1;
    private static final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        System.out.println("Сервер запущен...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новое соединение: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket, messageQueue);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String uniqueCode;
        private BlockingQueue<String> messageQueue;

        public ClientHandler(Socket socket, BlockingQueue<String> messageQueue) {
            this.socket = socket;
            this.messageQueue = messageQueue;
        }

        @Override
        public void run() {
            System.out.println("Новое соединение от: " + socket.getRemoteSocketAddress());
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                while (true) {
                    String command = in.readLine();
                    if (command == null) {
                        break;
                    }

                    System.out.println("Получена команда: " + command);

                    if ("REGISTER".equals(command)) {
                        handleRegister();
                    } else if ("LOGIN".equals(command)) {
                        handleLogin();
                    } else if ("CONFIRM_CODE".equals(command)) {
                        handleConfirmCode();
                    } else if ("CHAT".equals(command)) {
                        handleChat();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            new Thread(this::processMessages).start();
        }

        private void processMessages() {
            while (true) {
                try {
                    String message = messageQueue.take();
                    broadcastMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients.values()) {
                client.out.println(message);
            }
        }

        private void handleRegister() throws IOException {
            String username = in.readLine();
            String email = in.readLine();
            String password = in.readLine();

            if (DatabaseManager.checkUserExists(username, email)) {
                out.println("REGISTER_FAIL");
            } else {
                if (DatabaseManager.registerUser(username, email, hashPassword(password))) {
                    out.println("REGISTER_SUCCESS");
                } else {
                    out.println("REGISTER_FAIL");
                }
            }
        }

        private void handleLogin() throws IOException {
            String email = in.readLine();
            String password = in.readLine();

            String storedPassword = DatabaseManager.getPasswordByEmail(email);
            String hashedPassword = hashPassword(password);

            if (storedPassword != null && storedPassword.equals(hashedPassword)) {
                String code = generateConfirmationCode(email);
                MailSender.sendConfirmationCode(email, code);
                out.println("LOGIN_SUCCESS");
            } else {
                out.println("LOGIN_FAIL");
            }
        }


        private void handleConfirmCode() throws IOException {
            String email = in.readLine();
            String code = in.readLine();

            if (confirmationCodes.containsKey(email) && confirmationCodes.get(email).equals(code)) {
                confirmationCodes.remove(email);
                this.username = DatabaseManager.getUsernameByEmail(email); // Получаем имя пользователя по email
                this.uniqueCode = generateUniqueCode();
                out.println("CONFIRM_SUCCESS");
                out.println(username);
                out.println(uniqueCode);
                clients.put(uniqueCode, this);
                new Thread(this::processMessages).start();
            } else {
                out.println("CONFIRM_FAIL");
            }
        }



        private void handleChat() throws IOException {
            String userCode = in.readLine();
            String message = in.readLine();
            System.out.println("Получено сообщение от " + userCode + ": " + message);
            try {
                messageQueue.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        private String generateConfirmationCode(String email) {
            String code = String.valueOf(new Random().nextInt(899999) + 100000);
            confirmationCodes.put(email, code);
            return code;
        }

        private String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        private String generateUniqueCode() {
            return Base64.getEncoder().encodeToString(new byte[16]); // Генерирует случайный уникальный код
        }
    }
}
