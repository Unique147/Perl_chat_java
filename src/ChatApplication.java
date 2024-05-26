import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChatApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Создаем область для чата
        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);

        // Создаем список пользователей
        ListView<String> userList = new ListView<>();
        userList.getItems().addAll("Пользователь 1", "Пользователь 2", "Пользователь 3");

        // Создаем панель профиля
        Label profileLabel = new Label("Иконка профиля");
        HBox profileBox = new HBox(profileLabel);
        profileBox.setPadding(new Insets(10));

        // Располагаем компоненты на главной панели
        BorderPane root = new BorderPane();
        root.setLeft(chatArea);
        root.setRight(userList);
        root.setTop(profileBox);

        // Создаем сцену и отображаем окно
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Чат-приложение");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
