package com.example.instagram_project;

import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class JavaMailAPI extends AsyncTask<Void, Void, Void> {
    private String email, subject, message;

    public JavaMailAPI(String email, String subject, String message) {
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        final String senderEmail = "validator7112@gmail.com";
        final String senderPassword = "nvhc gvax yroh ncbs"; // 16-digit app password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message mm = new MimeMessage(session);
            mm.setFrom(new InternetAddress(senderEmail));
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            mm.setSubject(subject);
            mm.setText(message);
            Transport.send(mm);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
