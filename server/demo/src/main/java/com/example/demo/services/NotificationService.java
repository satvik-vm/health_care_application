package com.example.demo.services;

import com.example.demo.Entity.Notification;
import com.example.demo.Repository.NotificationRepository;
import com.example.demo.dto.Message;
import com.example.demo.dto.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

//    public void sendGlobalNotification() {
//        ResponseMessage message = new ResponseMessage("Global Notification");
//
//        messagingTemplate.convertAndSend("/topic/global-notifications", message);
//    }
//
    public void sendPrivateNotification(final Message message, final String senderId) {
        ResponseMessage msg = new ResponseMessage(message, senderId);

        messagingTemplate.convertAndSendToUser(message.getTo(),"/topic/private-notifications", msg);
    }

    public void createNotification(String sender, Message req) {
        Notification msg = new Notification();
        msg.setReceiver(req.getTo());
        msg.setMessage(req.getMessageContent());
        msg.setSender(sender);
        msg.setIsRead(false);
        msg.setTimestamp(LocalDateTime.now());
        msg.setDate(req.getDate());
        msg.setTime(req.getTime());
        notificationRepository.save(msg);
    }
}
