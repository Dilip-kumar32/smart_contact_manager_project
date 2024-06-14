package com.smart.service;


import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;


import java.util.Properties;

@Service
public class EmailService {

	public boolean sendEmail(String to,String subject,String text){
        boolean flag=false;

        Properties properties=new Properties();
        properties.put("mail.smtp.auth",true);
        properties.put("mail.smtp.starttls.enable",true);
        properties.put("mail.smtp.port","587");
        properties.put("mail.smtp.host","smtp.gmail.com");

        String username="coderdilip7";
        String password="xywf gygn rmyk ltix";
        //session
        Session session=Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try{
            Message message=new MimeMessage(session);
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setFrom(new InternetAddress("coderdilip7@gmail.com"));
            message.setSubject(subject);
//            message.setText(text);
            message.setContent(text, "text/html");

            Transport.send(message);
            flag=true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return flag;
    }
}
