package com.example.chat;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailSender {
    private static final String FROM = "perlchatjavasender@gmail.com";
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String PASSWORD = "qebdmtcodrcnrafv";

    public static void sendConfirmationCode(String email, String code) {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");  // включаем STARTTLS
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(
                props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM, PASSWORD);
                    }
                }
        );
        session.setDebug(true);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Код подтверждения");
            message.setText("Ваш код подтверждения: " + code);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}