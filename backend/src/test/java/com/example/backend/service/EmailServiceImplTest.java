package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.backend.exception.BusinessException;
import com.example.backend.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void sendPasswordResetEmailBuildsExpectedMessage() {
        EmailServiceImpl service = new EmailServiceImpl(mailSender, "no-reply@example.test", false);

        service.sendPasswordResetEmail("user@example.test", "http://localhost/reset?token=abc");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertThat(message.getFrom()).isEqualTo("no-reply@example.test");
        assertThat(message.getTo()).containsExactly("user@example.test");
        assertThat(message.getSubject()).contains("Reinitialisation");
        assertThat(message.getText()).contains("http://localhost/reset?token=abc");
    }

    @Test
    void sendPasswordResetEmailIgnoresMailErrorsWhenFailOnErrorIsFalse() {
        EmailServiceImpl service = new EmailServiceImpl(mailSender, "no-reply@example.test", false);
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        service.sendPasswordResetEmail("user@example.test", "http://localhost/reset?token=abc");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordResetEmailThrowsBusinessExceptionWhenFailOnErrorIsTrue() {
        EmailServiceImpl service = new EmailServiceImpl(mailSender, "no-reply@example.test", true);
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> service.sendPasswordResetEmail("user@example.test", "http://localhost/reset?token=abc"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Erreur lors de l'envoi de l'email.");
    }
}
