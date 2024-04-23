package com.example.demo.controllers;

import com.example.demo.dto.Message;
import com.example.demo.dto.ResponseMessage;
import com.example.demo.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@RestController
@CrossOrigin(origins="*")
public class MessageController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(NotificationService notificationService, SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public ResponseMessage getMessage(final Message message) throws InterruptedException {
        Thread.sleep(1000);
        notificationService.sendGlobalNotification();
        return new ResponseMessage(HtmlUtils.htmlEscape(message.getMessageContent()));
    }

    @MessageMapping("/private-message")
    @SendToUser("/topic/private-messages")
    public void getPrivateMessage(final Message message,
                                             final Principal principal) throws InterruptedException {
        Thread.sleep(1000);
        System.out.println(message.getTo());
        ResponseMessage response = new ResponseMessage(message.getMessageContent());
        notificationService.sendPrivateNotification(message.getTo());
        messagingTemplate.convertAndSendToUser(message.getTo(), "/topic/private-messages", response);
        notificationService.createNotification(principal.getName(), message.getTo(), message.getMessageContent());
    }
}
