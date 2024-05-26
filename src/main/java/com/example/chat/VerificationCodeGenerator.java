package com.example.chat;

import java.util.Random;

public class VerificationCodeGenerator {
    public static String generateVerificationCode() {
        // Генерация уникального кода
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
