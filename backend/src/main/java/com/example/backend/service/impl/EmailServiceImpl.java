package com.example.backend.service.impl;

import com.example.backend.exception.BusinessException;
import com.example.backend.service.interfaces.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String appMailFrom;

    public EmailServiceImpl(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String appMailFrom) {
        this.mailSender = mailSender;
        this.appMailFrom = appMailFrom;
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(appMailFrom);
        message.setTo(to);
        message.setSubject("Reinitialisation de votre mot de passe - SOPHATEL");
        message.setText("""
                Bonjour,

                Vous avez demande la reinitialisation de votre mot de passe.

                Cliquez sur le lien suivant pour creer un nouveau mot de passe :
                %s

                Ce lien expire dans 30 minutes.

                Si vous n'etes pas a l'origine de cette demande, ignorez cet email.

                Cordialement,
                SOPHATEL
                """.formatted(resetLink));

        try {
            LOGGER.info("Appel JavaMailSender.send pour email reset password to={} from={}", to, appMailFrom);
            mailSender.send(message);
            LOGGER.info("Email de reinitialisation envoye avec succes a {}", to);
        } catch (MailException exception) {
            LOGGER.error("Erreur lors de l'envoi email reset password", exception);
            throw new BusinessException("Erreur lors de l'envoi de l'email.", HttpStatus.BAD_REQUEST);
        }
    }
}
