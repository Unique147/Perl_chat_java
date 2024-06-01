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

public class ChatInterface extends Application {

    private Stage primaryStage;
    private Scene initialScene;
    private Scene loginScene;
    private Scene registerScene;
    private Scene chatScene;
    private String loggedInUsername;
    private String loggedInUniqueCode;

    private TextField loginField;
    private TextField emailField;
    private PasswordField passwordField;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

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

        root.add(registerButton, 0, 0);
        root.add(loginButton, 1, 0);

        initialScene = new Scene(root, 400, 300);
        initialScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        createLoginScene();
        createRegisterScene();

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
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
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

    private void loginUser(String email, String password) {
        try {
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("LOGIN");
            out.println(email);
            out.println(password);

            String response = in.readLine();
            if ("LOGIN_SUCCESS".equals(response)) {
                loggedInUsername = in.readLine();
                loggedInUniqueCode = in.readLine();
                createChatScene();
                primaryStage.setScene(chatScene);

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
            } else {
                showAlert("Ошибка входа", "Неправильный email или пароль.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerUser() {
        try {
            Socket socket = new Socket("localhost", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("REGISTER");
            out.println(loginField.getText());
            out.println(emailField.getText());
            out.println(passwordField.getText());

            String response = in.readLine();
            if ("REGISTER_SUCCESS".equals(response)) {
                showAlert("Успех", "Регистрация прошла успешно. Теперь вы можете войти.");
                primaryStage.setScene(loginScene);
            } else {
                showAlert("Ошибка регистрации", "Пользователь с таким именем или email уже существует.");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createChatScene() {
        VBox chatLayout = new VBox(10);
        chatLayout.setPadding(new Insets(10));

        Label chatLabel = new Label("Чат");
        ListView<String> chatListView = new ListView<>();
        TextField messageField = new TextField();
        messageField.setPromptText("Введите сообщение...");
        Button sendButton = new Button("Отправить");

        HBox messageBox = new HBox(10, messageField, sendButton);
        messageBox.setAlignment(Pos.CENTER);

        chatLayout.getChildren().addAll(chatLabel, chatListView, messageBox);

        sendButton.setOnAction(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                out.println("CHAT");
                out.println(loggedInUniqueCode);
                out.println(message);
                messageField.clear();
            }
        });

        chatScene = new Scene(chatLayout, 400, 300);
        chatScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    private void appendMessage(String message) {
        Platform.runLater(() -> {
            ListView<String> chatListView = (ListView<String>) chatScene.lookup(".list-view");
            chatListView.getItems().add(message);
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
