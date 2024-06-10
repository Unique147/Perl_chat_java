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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatInterface extends Application {

    private String currentEmail;
    private Scene confirmationScene;
    private TextField codeField;
    private SessionManager sessionManager;
    private Label loggedInLabel;
    private Stage primaryStage;
    private Scene initialScene;
    private Scene loginScene;
    private Scene registerScene;
    private Scene chatScene;
    private String loggedInUsername;
    private String loggedInUniqueCode;
    private TextField loginField;
    private TextField emailField;
    private PasswordField passwordFieldRegister;
    private PasswordField passwordFieldLogin;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private TextArea chatTextArea;
    private TextField messageField;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(); // Final переменная

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        sessionManager = new SessionManager(this);

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
        root.add(registerButton, 0, 1);
        root.add(loginButton, 1, 1);
        root.add(titleLabel, 0, 0, 2, 1);
        GridPane.setHalignment(titleLabel, Pos.CENTER.getHpos());
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
        passwordFieldRegister = new PasswordField();
        passwordFieldRegister.setPromptText("Пароль");
        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");
        registerLayout.add(registerLabel, 0, 0, 2, 1);
        registerLayout.add(loginField, 0, 1);
        registerLayout.add(emailField, 0, 2);
        registerLayout.add(passwordFieldRegister, 0, 3);
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
        passwordFieldLogin = new PasswordField();
        passwordFieldLogin.setPromptText("Пароль");
        Button loginButton = new Button("Войти");
        Button backButton = new Button("Назад");
        loginLayout.add(loginLabel, 0, 0, 2, 1);
        loginLayout.add(emailField, 0, 1);
        loginLayout.add(passwordFieldLogin, 0, 2);
        loginLayout.add(loginButton, 0, 3);
        loginLayout.add(backButton, 1, 3);
        loginScene = new Scene(loginLayout, 400, 300);
        loginScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        backButton.setOnAction(e -> primaryStage.setScene(initialScene));
        loginButton.setOnAction(e -> loginUser(emailField.getText(), passwordFieldLogin.getText()));
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

            socket = new Socket("192.168.1.68", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("LOGIN");
            out.println(email);
            out.println(password);
            String response = in.readLine();
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

    private void registerUser() {
        try {
            Socket socket = new Socket("192.168.1.68", 12345);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("REGISTER");
            out.println(loginField.getText());
            out.println(emailField.getText());
            out.println(passwordFieldRegister.getText());
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

    private void confirmCode() {
        try {
            // Отправка кода подтверждения на сервер
            out.println("CONFIRM_CODE");
            out.println(currentEmail);
            out.println(codeField.getText());

            // Чтение ответа от сервера
            String response = in.readLine();
            if ("CONFIRM_SUCCESS".equals(response)) {
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
                // Начало сессии
                sessionManager.startSession(loggedInUniqueCode);
            } else {
                showAlert("Ошибка подтверждения", "Неправильный код подтверждения.");
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
                out.println(loggedInUsername + ": " + message); // Format message before sending
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