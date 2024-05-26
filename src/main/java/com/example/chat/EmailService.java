package com.example.chat;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    public void sendVerificationCode(String recipientEmail, String verificationCode) {
        String subject = "Код подтверждения для регистрации";
        String messageText = "Ваш код подтверждения для регистрации: " + verificationCode;

        // Отправка сообщения
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@example.com")); // Замените на ваш адрес электронной почты
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);

            System.out.println("Код подтверждения отправлен на вашу почту.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

