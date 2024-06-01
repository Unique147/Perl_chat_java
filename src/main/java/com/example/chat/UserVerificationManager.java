/*package com.example.chat;

import java.util.HashMap;
import java.util.Map;

public class UserVerificationManager {
    private static Map<String, String> verificationCodes = new HashMap<>(); // Хранение кодов подтверждения по email

    public static void addVerificationCode(String email, String verificationCode) {
        verificationCodes.put(email, verificationCode);
    }

    public static boolean verifyCode(String email, String enteredCode) {
        String storedCode = verificationCodes.get(email);
        return storedCode != null && storedCode.equals(enteredCode);
    }

    public static void removeVerificationCode(String email) {
        verificationCodes.remove(email);
    }
}
*/