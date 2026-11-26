
package com.jobpilot.email.integration;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;

@Component
public class GmailMimeMessageBuilder {

    public String buildRawMessage(
            String senderEmail,
            String recipientEmail,
            String subject,
            String body) {

        try {
            Session session = Session.getInstance(new Properties());

            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(senderEmail));

            message.setRecipient(
                    Message.RecipientType.TO,
                    new InternetAddress(recipientEmail)
            );

            message.setSubject(subject, "UTF-8");

            message.setText(body, "UTF-8");

            message.saveChanges();

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            message.writeTo(outputStream);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(outputStream.toByteArray());

        } catch (MessagingException | IOException exception) {

            throw new IllegalStateException(
                    "Failed to build Gmail MIME message.",
                    exception
            );
        }
    }
}
