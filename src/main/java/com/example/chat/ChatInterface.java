package com.example.chat;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;


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

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase(); // Инициализация базы данных
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Создаем кнопки "Зарегистрироваться" и "Войти"
        Button registerButton = new Button("Зарегистрироваться");
        Button loginButton = new Button("Войти");
        registerButton.getStyleClass().add("button");
        loginButton.getStyleClass().add("button");

        // Обработчики событий для кнопок
        registerButton.setOnAction(e -> {
            primaryStage.setScene(registerScene);
        });

        loginButton.setOnAction(e -> primaryStage.setScene(loginScene));

        // Создаем вертикальный контейнер для размещения кнопок
        GridPane root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(10);
        root.setVgap(10);
        root.setPadding(new Insets(25, 25, 25, 25));

        root.add(registerButton, 0, 0);
        root.add(loginButton, 1, 0);

        // Устанавливаем кнопки по центру сетки
        GridPane.setHalignment(registerButton, HPos.CENTER);
        GridPane.setHalignment(loginButton, HPos.CENTER);

        // Создаем сцену и устанавливаем контейнер в качестве корневого узла
        initialScene = new Scene(root, 400, 300);
        initialScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        root.getStyleClass().add("root");

        // Создаем сцены для страницы входа и страницы регистрации
        createLoginScene();
        createRegisterScene();

        // Устанавливаем сцену для основного окна
        primaryStage.setScene(initialScene);
        primaryStage.setTitle("Чат");
        primaryStage.show();
    }

    // Метод для отображения страницы регистрации
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

        loginField.getStyleClass().add("text-field");
        emailField.getStyleClass().add("text-field");
        passwordField.getStyleClass().add("password-field");

        registerLabel.getStyleClass().add("label");

        Button registerButton = new Button("Зарегистрироваться");
        Button backButton = new Button("Назад");
        registerButton.getStyleClass().add("button");
        backButton.getStyleClass().add("button");

        registerButton.setOnAction(e -> {
            String username = loginField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String uniqueCode = UUID.randomUUID().toString(); // Генерация уникального кода
            String verificationCode = VerificationCodeGenerator.generateVerificationCode(); // Генерация кода подтверждения

            if (registerUser(username, email, password, uniqueCode, verificationCode)) {
                System.out.println("Вы зарегистрировались!");
                primaryStage.setScene(initialScene);
            } else {
                System.out.println("Ошибка при регистрации.");
            }
        });

        backButton.setOnAction(e -> primaryStage.setScene(initialScene));

        registerLayout.add(registerLabel, 0, 0);
        registerLayout.add(loginField, 0, 1);
        registerLayout.add(emailField, 0, 2);
        registerLayout.add(passwordField, 0, 3);
        registerLayout.add(registerButton, 0, 4);
        registerLayout.add(backButton, 0, 5);

        // Устанавливаем компоненты по центру сетки
        for (int i = 0; i < 6; i++) {
            GridPane.setHalignment(registerLayout.getChildren().get(i), HPos.CENTER);
        }

        registerScene = new Scene(registerLayout, 400, 300);
        registerScene.getStylesheets().add("styles.css");
        registerLayout.getStyleClass().add("border-pane");
    }

    private boolean registerUser(String username, String email, String password, String uniqueCode, String verificationCode) {
        String sqlCheck = "SELECT * FROM users WHERE username = ? OR email = ?";
        String sqlInsert = "INSERT INTO users(username, email, password, unique_code, verification_code) VALUES(?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck);
             PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {

            // Проверка на существование пользователя с таким же username или email
            pstmtCheck.setString(1, username);
            pstmtCheck.setString(2, email);
            ResultSet rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                // Пользователь с таким username или email уже существует
                System.out.println("Пользователь с таким логином или email уже существует.");
                return false;
            }

            // Вставка нового пользователя
            pstmtInsert.setString(1, username);
            pstmtInsert.setString(2, email);
            pstmtInsert.setString(3, password);
            pstmtInsert.setString(4, uniqueCode);
            pstmtInsert.setString(5, verificationCode);
            pstmtInsert.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    // Метод для отображения страницы входа
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
        Label loginMessage = new Label();

        emailField.getStyleClass().add("text-field");
        passwordField.getStyleClass().add("password-field");
        loginLabel.getStyleClass().add("label");
        loginMessage.getStyleClass().add("label");

        Button loginButton = new Button("Войти");
        Button backButton = new Button("Назад");
        loginButton.getStyleClass().add("button");
        backButton.getStyleClass().add("button");

        loginButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (authenticateUser(email, password)) {
                System.out.println("Вы вошли!");
                createChatScene();  // Обновляем сцену чата с информацией о пользователе
                primaryStage.setScene(chatScene);
            } else {
                loginMessage.setText("Неверные данные.");
            }
        });

        backButton.setOnAction(e -> primaryStage.setScene(initialScene));

        loginLayout.add(loginLabel, 0, 0);
        loginLayout.add(emailField, 0, 1);
        loginLayout.add(passwordField, 0, 2);
        loginLayout.add(loginButton, 0, 3);
        loginLayout.add(backButton, 0, 4);
        loginLayout.add(loginMessage, 0, 5);

        // Устанавливаем компоненты по центру сетки
        for (int i = 0; i < 6; i++) {
            GridPane.setHalignment(loginLayout.getChildren().get(i), HPos.CENTER);
        }

        loginScene = new Scene(loginLayout, 400, 300);
        loginScene.getStylesheets().add("styles.css");
        loginLayout.getStyleClass().add("border-pane");
    }

    // Метод для отображения страницы чата
    // Метод для отображения страницы чата
    private void createChatScene() {
        BorderPane chatLayout = new BorderPane();
        chatLayout.setPadding(new Insets(10, 10, 10, 10));
        chatLayout.getStyleClass().add("border-pane");

        // Верхняя панель с кнопкой выхода, логином и уникальным кодом пользователя
        HBox topPanel = new HBox();
        topPanel.setSpacing(10);
        topPanel.setAlignment(Pos.CENTER_RIGHT); // Выравниваем по правому краю
        topPanel.getStyleClass().add("top-panel");

        Label userInfo = new Label();
        userInfo.getStyleClass().add("label");
        Button logoutButton = new Button("Выйти");
        logoutButton.getStyleClass().add("button");
        logoutButton.setOnAction(e -> primaryStage.setScene(initialScene));
        userInfo.setText("Логин: " + loggedInUsername + " | Код: " + loggedInUniqueCode);

        // Добавляем обработчик событий для метки с уникальным кодом
        userInfo.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(loggedInUniqueCode);
                clipboard.setContent(content);
                System.out.println("Код скопирован: " + loggedInUniqueCode);
            }
        });

        topPanel.getChildren().addAll(userInfo, logoutButton);

        // Центр для области сообщений
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.getStyleClass().add("chat-area");

        // Нижняя панель для ввода сообщения и кнопки отправки
        HBox bottomPanel = new HBox();
        bottomPanel.setSpacing(10);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setPadding(new Insets(10, 0, 0, 0));
        bottomPanel.getStyleClass().add("bottom-panel");

        TextField messageField = new TextField();
        messageField.setPromptText("Введите сообщение...");
        messageField.getStyleClass().add("message-field");
        Button sendButton = new Button("Отправить");
        sendButton.getStyleClass().add("button");
        sendButton.setOnAction(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                chatArea.appendText(loggedInUsername + ": " + message + "\n");
                messageField.clear();
            }
        });

        bottomPanel.getChildren().addAll(messageField, sendButton);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        // Левая панель для списка чатов
        VBox chatListContainer = new VBox();
        chatListContainer.setSpacing(10);
        chatListContainer.setPadding(new Insets(10, 0, 10, 0));
        chatListContainer.getStyleClass().add("chat-list-container");

        Label chatListLabel = new Label("Чаты");
        chatListLabel.getStyleClass().add("label");

        Button addChatButton = new Button("+");
        addChatButton.getStyleClass().add("button");
        addChatButton.setOnAction(e -> openAddChatDialog(chatArea));

        chatListContainer.getChildren().addAll(chatListLabel, addChatButton);
        chatListContainer.setPrefWidth(150);

        // Устанавливаем компоненты на BorderPane
        chatLayout.setTop(topPanel);
        chatLayout.setCenter(chatArea);
        chatLayout.setBottom(bottomPanel);
        chatLayout.setLeft(chatListContainer);

        chatScene = new Scene(chatLayout, 600, 400);
        chatScene.getStylesheets().add("styles.css");
    }


    private void openAddChatDialog(TextArea chatArea) {
        Stage dialog = new Stage();
        dialog.setTitle("Добавить собеседника");

        GridPane dialogLayout = new GridPane();
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setHgap(10);
        dialogLayout.setVgap(10);
        dialogLayout.setPadding(new Insets(25, 25, 25, 25));

        Label codeLabel = new Label("Уникальный код собеседника:");
        TextField codeField = new TextField();
        codeField.setPromptText("Введите уникальный код");
        Button connectButton = new Button("Подключиться");
        Button cancelButton = new Button("Отмена");

        connectButton.setOnAction(e -> {
            String uniqueCode = codeField.getText();
            if (!uniqueCode.isEmpty()) {
                String username = getUsernameByUniqueCode(uniqueCode);
                if (username != null) {
                    chatArea.appendText("Чат с " + username + "\n");
                    dialog.close();
                } else {
                    showAlert("Ошибка", "Собеседник не найден");
                }
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        dialogLayout.add(codeLabel, 0, 0);
        dialogLayout.add(codeField, 0, 1);
        dialogLayout.add(connectButton, 0, 2);
        dialogLayout.add(cancelButton, 0, 3);

        Scene dialogScene = new Scene(dialogLayout, 300, 200);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private String getUsernameByUniqueCode(String uniqueCode) {
        String sql = "SELECT username FROM users WHERE unique_code = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uniqueCode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Метод для аутентификации пользователя
    private boolean authenticateUser(String email, String password) {
        String sql = "SELECT username, unique_code FROM users WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                loggedInUsername = rs.getString("username");
                loggedInUniqueCode = rs.getString("unique_code");
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}