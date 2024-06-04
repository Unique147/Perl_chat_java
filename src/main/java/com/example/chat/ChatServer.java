package com.example.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ChatServer {

    private static final int PORT = 12345;
    private static HashMap<String, ClientHandler> clients = new HashMap<>();
    private static ArrayList<User> registeredUsers = new ArrayList<>();
    private static int nextUniqueCode = 1;
    private static BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

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

        public ClientHandler(Socket socket, BlockingQueue<String> messageQueue) { // Убрали TextArea
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
                    broadcastMessage(message); // Broadcast the message to all clients
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

            for (User user : registeredUsers) {
                if (user.getUsername().equals(username) || user.getEmail().equals(email)) {
                    out.println("REGISTER_FAIL");
                    return;
                }
            }

            User newUser = new User(username, email, password);
            registeredUsers.add(newUser);
            out.println("REGISTER_SUCCESS");
        }

        private void handleLogin() throws IOException {
            String email = in.readLine();
            String password = in.readLine();

            for (User user : registeredUsers) {
                if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                    this.username = user.getUsername();
                    this.uniqueCode = generateUniqueCode();
                    out.println("LOGIN_SUCCESS");
                    out.println(username);
                    out.println(uniqueCode);
                    clients.put(uniqueCode, this);
                    new Thread(this::processMessages).start();  // Start the message processing thread here after login
                    return;
                }
            }

            out.println("LOGIN_FAIL");
        }

        private void handleChat() throws IOException {
            String userCode = in.readLine();
            String message = in.readLine();
            System.out.println("Получено сообщение от " + userCode + ": " + message);
            try {
                messageQueue.put(message); // Add message to the queue
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private String generateUniqueCode() {
            return String.valueOf(nextUniqueCode++);
        }
    }

    private static class User {
        private String username;
        private String email;
        private String password;

        public User(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
