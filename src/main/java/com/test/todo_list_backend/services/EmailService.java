package com.test.todo_list_backend.services;

import com.sendgrid.helpers.mail.Mail;

public interface EmailService {

    void sendEmail(Mail mail);
    Mail createEmailTemplate(String emailTo, String fullName, String code, String subject);
}
