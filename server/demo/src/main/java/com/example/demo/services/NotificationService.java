package com.example.demo.services;

import com.example.demo.Entity.Notification;
import com.example.demo.Repository.NotificationRepository;
import com.example.demo.dto.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendGlobalNotification() {
        ResponseMessage message = new ResponseMessage("Global Notification");

        messagingTemplate.convertAndSend("/topic/global-notifications", message);
    }

    public void sendPrivateNotification(final String userId) {
        ResponseMessage message = new ResponseMessage("Private Notification");

        messagingTemplate.convertAndSendToUser(userId,"/topic/private-notifications", message);
    }

    public void createNotification(String sender, String to, String messageContent) {
        Notification msg = new Notification();
        msg.setReceiver(to);
        msg.setMessage(messageContent);
        msg.setSender(sender);
        msg.setIsRead(false);
        msg.setTimestamp(Date.from(java.time.Instant.now()));
        notificationRepository.save(msg);
    }
}
