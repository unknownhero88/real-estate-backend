package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public ChatWebSocketController(
            SimpMessagingTemplate messagingTemplate,
            ChatRepository chatRepository,
            UserRepository userRepository,
            PropertyRepository propertyRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    public static class WsChatMessage {
        public Long receiverId;
        public Long propertyId;
        public String content;
    }

    @MessageMapping("/chat.send")
    @Transactional
    public void sendMessage(WsChatMessage msg, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User receiver = userRepository.findById(msg.receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Property property = propertyRepository.findById(msg.propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        // Save message to database
        ChatMessage savedMessage = chatRepository.save(
                new ChatMessage(sender, receiver, property, msg.content)
        );

        // Prepare payload
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", savedMessage.getId());
        payload.put("senderId", sender.getId());
        payload.put("senderName", sender.getFullName());
        payload.put("receiverId", receiver.getId());
        payload.put("propertyId", property.getId());
        payload.put("propertyTitle", property.getTitle());
        payload.put("content", savedMessage.getContent());
        payload.put("isRead", false);
        payload.put("sentAt", savedMessage.getSentAt() != null
                ? savedMessage.getSentAt().toString()
                : LocalDateTime.now().toString());

        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                payload
        );

        // Echo back to sender
        messagingTemplate.convertAndSendToUser(
                sender.getEmail(),
                "/queue/messages",
                payload
        );
    }

    // Typing indicator
    @MessageMapping("/chat.typing")
    public void typing(Map<String, Object> data, Principal principal) {
        Long receiverId = Long.parseLong(data.get("receiverId").toString());
        User receiver = userRepository.findById(receiverId).orElse(null);
        if (receiver == null) return;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("typingUser", principal.getName());
        payload.put("isTyping", data.get("isTyping"));

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/typing",
                payload
        );
    }
}