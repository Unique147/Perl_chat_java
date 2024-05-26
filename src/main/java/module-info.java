module com.example.chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires java.sql;
    requires java.desktop;


    opens com.example.chat to javafx.fxml;
    exports com.example.chat;
}