package com.example.demo.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.example.demo.dto.Message;
import com.example.demo.dto.ResponseMessage;
import com.example.demo.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketService {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private final SimpMessagingTemplate messagingTemplate;

    public void sendSocketMessage(SocketIOClient senderClient, Message message, String room, String receiver) {
        String sender = senderClient.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
        ResponseMessage response = new ResponseMessage(message, sender);
        notificationService.sendPrivateNotification(message, sender);
        notificationService.createNotification(sender, message);

        for (SocketIOClient client : senderClient.getNamespace().getRoomOperations(room).getClients()) {
            String email = client.getHandshakeData().getUrlParams().get("email").stream().collect(Collectors.joining());
            if (email.equals(receiver)) {
                client.sendEvent("read_message", response);
            }
        }
    }

    public void saveMessage(SocketIOClient senderClient, Message message) {
//        Message storedMessage = messageService.saveMessage(Message.builder()
//                .messageType(MessageType.CLIENT)
//                .content(message.getMessageContent())
//                .room(message.getRoom())
//                .username(message.getUsername())
//                .build());
        String room = senderClient.getHandshakeData().getUrlParams().get("room").stream().collect(Collectors.joining());
        sendSocketMessage(senderClient, message, room, message.getTo());
    }

//    public void saveInfoMessage(SocketIOClient senderClient, String message, String room) {
//        Message storedMessage = messageService.saveMessage(Message.builder()
//                .messageType(MessageType.SERVER)
//                .content(message)
//                .room(room)
//                .build());
//        sendSocketMessage(senderClient, storedMessage, room);
    }
