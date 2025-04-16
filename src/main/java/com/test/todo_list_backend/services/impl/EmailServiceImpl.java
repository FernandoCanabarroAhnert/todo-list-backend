package com.test.todo_list_backend.services.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.test.todo_list_backend.services.EmailService;
import com.test.todo_list_backend.services.exceptions.EmailException;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private SendGrid sendGrid;
    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    private String emailFrom = "ahnertfernando499@gmail.com";

    @Override
    public void sendEmail(Mail mail) {
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                throw new EmailException(response.getBody());
            }
        }
        catch (IOException e) {
            throw new EmailException(e.getMessage());
        }
    }

    @Override
    public Mail createEmailTemplate(String emailTo, String fullName, String code, String subject) {
        Map<String, Object> variables = Map.of(
            "username", fullName,
            "code", code
        );
        Context context = new Context();
        context.setVariables(variables);

        String template = springTemplateEngine.process(
            "email_confirmation",
            context
        );

        Email from = new Email(emailFrom, "Todo List App");
        Email to = new Email(emailTo);
        Content content = new Content("text/html", template);

        return new Mail(from, subject, to, content);
    }

}
