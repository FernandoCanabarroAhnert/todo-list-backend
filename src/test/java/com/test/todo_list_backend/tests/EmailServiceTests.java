package com.test.todo_list_backend.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.test.todo_list_backend.services.exceptions.EmailException;
import com.test.todo_list_backend.services.impl.EmailServiceImpl;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTests {

    @InjectMocks
    private EmailServiceImpl emailService;
    @Mock
    private SendGrid sendGrid;
    @Mock
    private SpringTemplateEngine springTemplateEngine;

    @Test
    public void sendEmailShouldThrowNoException() throws IOException {
        Mail mail = new Mail();
        Response response = new Response();
        response.setStatusCode(200);
        when(sendGrid.api(any(Request.class))).thenReturn(response);
        emailService.sendEmail(mail);
        verify(sendGrid).api(any(Request.class));
    }

    @Test
    public void sendEmailShouldThrowEmailExceptionWhenResponseStatusIsError() throws IOException {
        Mail mail = new Mail();
        Response response = new Response();
        response.setStatusCode(400);
        when(sendGrid.api(any(Request.class))).thenReturn(response);
        assertThatThrownBy(() -> emailService.sendEmail(mail)).isInstanceOf(EmailException.class);
    }

    @Test
    public void sendEmailShouldThrowEmailExceptionWhenIOExceptionIsThrown() throws IOException {
        Mail mail = new Mail();
        when(sendGrid.api(any(Request.class))).thenThrow(IOException.class);
        assertThatThrownBy(() -> emailService.sendEmail(mail)).isInstanceOf(EmailException.class);
    }

    @Test
    public void testCreateConfirmationEmailTemplate() {
        String emailTo = "email";
        String username = "username";
        String code = "code";
        String subject = "subject";

        Map<String,Object> variables = Map.of(
            "username", username,
            "code", code
        );
        Context context = new Context();
        context.setVariables(variables);
        when(springTemplateEngine.process(eq("email_confirmation"), any(Context.class))).thenReturn("content");
        Mail mail = emailService.createEmailTemplate(emailTo, username, code, subject);
        assertThat(mail).isNotNull();
        assertThat(mail.getFrom().getEmail()).isEqualTo("ahnertfernando499@gmail.com");
        assertThat(mail.getContent().get(0).getValue()).isEqualTo("content");
    }

}
