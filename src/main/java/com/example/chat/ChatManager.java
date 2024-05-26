/*package com.example.chat;

import javafx.scene.control.TextArea;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatManager {
    public static int createOrGetChat(String user1, String user2) {
        String sqlCheck = "SELECT chat_id FROM chats WHERE (user1 = ? AND user2 = ?) OR (user1 = ? AND user2 = ?)";
        String sqlInsert = "INSERT INTO chats(user1, user2) VALUES(?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck);
             PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {

            pstmtCheck.setString(1, user1);
            pstmtCheck.setString(2, user2);
            pstmtCheck.setString(3, user2);
            pstmtCheck.setString(4, user1);
            ResultSet rs = pstmtCheck.executeQuery();

            if (rs.next()) {
                return rs.getInt("chat_id");
            } else {
                pstmtInsert.setString(1, user1);
                pstmtInsert.setString(2, user2);
                pstmtInsert.executeUpdate();
                ResultSet generatedKeys = pstmtInsert.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void saveMessage(int chatId, String sender, String message) {
        String sql = "INSERT INTO messages(chat_id, sender, message) VALUES(?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chatId);
            pstmt.setString(2, sender);
            pstmt.setString(3, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadChatMessages(int chatId, TextArea chatArea) {
        String sql = "SELECT sender, message, timestamp FROM messages WHERE chat_id = ? ORDER BY timestamp";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, chatId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");
                chatArea.appendText("[" + timestamp + "] " + sender + ": " + message + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getUsernameByUniqueCode(String uniqueCode) {
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
}*/
