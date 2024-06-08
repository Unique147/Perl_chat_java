package com.example.chat;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatInterface extends Application {

    private Stage primaryStage;
    private Scene initialScene;
    private Scene loginScene;
    private Scene registerScene;
    private Scene confirmationScene;
    private Scene chatScene;
    private String loggedInUsername;
    private String loggedInUniqueCode;
    private String currentEmail;

    private TextField loginField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField codeField;
    private Label loggedInLabel;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private TextArea chatTextArea;
    private TextField messageField;
    private SessionManager sessionManager;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sessionManager = new SessionManager(this);

        Label titleLabel = new Label("Perl chat");
        titleLabel.setStyle("-fx-font-size: 60px; -fx-text-fill: #6200EA;");

        Button registerButton = new Button("Зарегистрироваться");
        Button loginButton = new Button("Войти");
        registerButton.getStyleClass().add("button");
        loginButton.getStyleClass().add("button");

        registerButton.setOnAction(e -> primaryStage.setScene(registerScene));
        loginButton.setOnAction(e -> primaryStage.setScene(loginScene));

        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25, 25, 25, 25));

        root.add(titleLabel, 0, 0, 2, 1);
        GridPane.setHalignment(titleLabel, Pos.CENTER.getHpos());

        root.add(registerButton, 0, 1);
        root.add(loginButton, 1, 1);


        initialScene = new Scene(root, 400, 300);
        initialScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        createLoginScene();
        createRegisterScene();
        createConfirmationScene();

        primaryStage.setScene(initialScene);
        primaryStage.setTitle("Чат");
        primaryStage.show();
    }

    private void createRegisterScene() {
        GridPane registerLayout = new GridPane();
        registerLayout.setAlignment(Pos.CENTER);
        registerLayout.setHgap(10);
        registerLayout.setVgap(10);
        registerLayout.setPadding(new Insets(25, 25, 25, 25));

        Label registerLabel = new Label("Регистрация");
        loginField = new TextField();
        loginField.setPromptText("Логин");
        emailField = new TextField();
        emailField.setPromptText("Email");
        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");

        registerLayout.add(registerLabel, 0, 0, 2, 1);
        registerLayout.add(loginField, 0, 1);
        registerLayout.add(emailField, 0, 2);
        registerLayout.add(passwordField, 0, 3);
        registerLayout.add(registerButton, 0, 4);
        registerLayout.add(backButton, 1, 4);

        registerScene = new Scene(registerLayout, 400, 300);
        registerScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        backButton.setOnAction(e -> primaryStage.setScene(initialScene));
        registerButton.setOnAction(e -> registerUser());
    }

    private void createLoginScene() {
        GridPane loginLayout = new GridPane();
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setHgap(10);
        loginLayout.setVgap(10);
        loginLayout.setPadding(new Insets(25, 25, 25, 25));

        Label loginLabel = new Label("Вход");
        emailField = new TextField();
        emailField.setPromptText("Email");
        passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");
        Button loginButton = new Button("Войти");
        Button backButton = new Button("Назад");

        loginLayout.add(loginLabel, 0, 0, 2, 1);
        loginLayout.add(emailField, 0, 1);
        loginLayout.add(passwordField, 0, 2);
        loginLayout.add(loginButton, 0, 3);
        loginLayout.add(backButton, 1, 3);

        loginScene = new Scene(loginLayout, 400, 300);
        loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        backButton.setOnAction(e -> primaryStage.setScene(initialScene));
        loginButton.setOnAction(e -> loginUser(emailField.getText(), passwordField.getText()));
    }

    private void createConfirmationScene() {
        GridPane confirmationLayout = new GridPane();
        confirmationLayout.setAlignment(Pos.CENTER);
        confirmationLayout.setHgap(10);
        confirmationLayout.setVgap(10);
        confirmationLayout.setPadding(new Insets(25, 25, 25, 25));

        Label confirmationLabel = new Label("Подтверждение");
        codeField = new TextField();
        codeField.setPromptText("Введите код");
        Button confirmButton = new Button("Продолжить");
        Button backButton = new Button("Назад");

        confirmationLayout.add(confirmationLabel, 0, 0, 2, 1);
        confirmationLayout.add(codeField, 0, 1);
        confirmationLayout.add(confirmButton, 0, 2);
        confirmationLayout.add(backButton, 1, 2);

        confirmationScene = new Scene(confirmationLayout, 400, 300);
        confirmationScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        backButton.setOnAction(e -> primaryStage.setScene(loginScene));
        confirmButton.setOnAction(e -> confirmCode());
    }

    private void loginUser(String email, String password) {
        try {
            // Хэшируем пароль, введенный пользователем
            String hashedPassword = hashPassword(password);

            // Устанавливаем соединение с сервером
            socket = new Socket("192.168.1.65", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Отправляем серверу команду "LOGIN", email и хэш пароля
            out.println("LOGIN");
            out.println(email);
            out.println(hashedPassword);

            // Читаем ответ от сервера
            String response = in.readLine();


            // Проверяем ответ от сервера
            if ("LOGIN_SUCCESS".equals(response)) {
                currentEmail = email;
                primaryStage.setScene(confirmationScene);
            } else {
                showAlert("Ошибка входа", "Неправильный email или пароль.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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


    private void confirmCode() {
        try {
            // Отправка кода подтверждения на сервер
            out.println("CONFIRM_CODE");
            out.println(currentEmail);
            out.println(codeField.getText());

            // Чтение ответа от сервера
            String response = in.readLine();
            if ("CONFIRM_SUCCESS".equals(response)) {
                loggedInUsername = in.readLine(); // Считываем логин пользователя с сервера
                loggedInUniqueCode = in.readLine(); // Считываем уникальный код
                createChatScene();
                primaryStage.setScene(chatScene);

                // Запуск нового потока для чтения сообщений из сервера
                new Thread(() -> {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            appendMessage(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                // Начало сессии
                sessionManager.startSession(loggedInUniqueCode);
            } else {
                showAlert("Ошибка подтверждения", "Неправильный код подтверждения.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void registerUser() {
        try {
            // Установка соединения с сервером
            socket = new Socket("192.168.1.65", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String hashedPassword = hashPassword(passwordField.getText());

            // Отправка данных для регистрации, включая соль
            out.println("REGISTER");
            out.println(loginField.getText());
            out.println(emailField.getText());
            out.println(hashedPassword);


            // Чтение ответа от сервера
            String response = in.readLine();
            if ("REGISTER_SUCCESS".equals(response)) {
                showAlert("Успех", "Регистрация прошла успешно.");
                primaryStage.setScene(loginScene);
            } else {
                showAlert("Ошибка регистрации", "Пользователь с таким именем или email уже существует.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showConfirmationScene(String userCode) {
        Platform.runLater(() -> {
            primaryStage.setScene(confirmationScene);
            sessionManager.startConfirmationTimer(userCode);

        });
    }

    public void logoutUser() {
        Platform.runLater(() -> {
            sessionManager.stopSession(loggedInUniqueCode);
            primaryStage.setScene(initialScene);
            showAlert("Сеанс завершен", "Вы были разлогинены.");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            loggedInUsername = null;
            loggedInUniqueCode = null;
            currentEmail = null;

            loggedInLabel.setText("");
        });
    }

    private void createChatScene() {
        VBox chatLayout = new VBox(10);
        chatLayout.setPadding(new Insets(10));

        chatTextArea = new TextArea();
        chatTextArea.setEditable(false);
        chatTextArea.setWrapText(true);
        chatTextArea.setStyle("-fx-control-inner-background: white;");

        Label chatLabel = new Label("Чат");

        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(event -> logoutUser());

        loggedInLabel = new Label("Пользователь: " + loggedInUsername);
        loggedInLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6200EA;");

        messageField = new TextField();
        messageField.setPromptText("Введите сообщение...");
        Button sendButton = new Button("Отправить");

        HBox messageBox = new HBox(10, messageField, sendButton);
        messageBox.setAlignment(Pos.CENTER);

        HBox topBox = new HBox(10, loggedInLabel, logoutButton);
        topBox.setAlignment(Pos.CENTER_RIGHT);

        chatLayout.getChildren().addAll(topBox, chatLabel, chatTextArea, messageBox);

        sendButton.setOnAction(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                out.println("CHAT");
                out.println(loggedInUniqueCode);
                out.println(loggedInUsername + ": " + message);
                messageField.clear();
            }
        });

        new Thread(() -> {
            try {
                String message;
                while ((message = messageQueue.take()) != null) {
                    String finalMessage = message;
                    Platform.runLater(() -> chatTextArea.appendText(finalMessage + "\n"));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        chatScene = new Scene(chatLayout, 400, 300);
        chatScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }


    private void appendMessage(String message) {
        Platform.runLater(() -> chatTextArea.appendText(message + "\n"));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}