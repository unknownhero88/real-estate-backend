package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.Property;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ChatRepository;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class ChatController {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public ChatController(ChatRepository chatRepository,
                          UserRepository userRepository,
                          PropertyRepository propertyRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    public static class SendMessageRequest {
        @NotNull public Long receiverId;
        @NotNull public Long propertyId;
        @NotBlank public String content;
    }

    // Send a new message
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendMessage(
            @RequestBody SendMessageRequest req,
            @AuthenticationPrincipal UserDetails ud) {

        User sender = getUser(ud);
        User receiver = userRepository.findById(req.receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        Property property = propertyRepository.findById(req.propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        ChatMessage message = new ChatMessage(sender, receiver, property, req.content);
        chatRepository.save(message);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Message sent", toMap(message))
        );
    }

    // Get conversation between buyer and seller for a property
    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getConversation(
            @RequestParam Long propertyId,
            @RequestParam Long otherUserId,
            @AuthenticationPrincipal UserDetails ud) {

        Long myId = getUser(ud).getId();
        List<ChatMessage> messages = chatRepository.findConversation(propertyId, myId, otherUserId);

        // Mark as read
        chatRepository.markConversationRead(myId, otherUserId, propertyId);

        return ResponseEntity.ok(ApiResponse.success(
                "Conversation fetched",
                messages.stream().map(this::toMap).collect(Collectors.toList())
        ));
    }

    // Get all chat threads for current user
    @GetMapping("/threads")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getThreads(
            @AuthenticationPrincipal UserDetails ud) {

        Long myId = getUser(ud).getId();
        List<ChatMessage> allMessages = chatRepository.findAllForUser(myId);

        // Group by property + other user
        Map<String, ChatMessage> latest = new LinkedHashMap<>();
        for (ChatMessage m : allMessages) {
            Long otherId = m.getSender().getId().equals(myId)
                    ? m.getReceiver().getId()
                    : m.getSender().getId();
            String key = m.getProperty().getId() + "-" + otherId;
            latest.putIfAbsent(key, m);
        }

        List<Map<String, Object>> threads = latest.values().stream()
                .map(m -> {
                    Map<String, Object> t = toMap(m);
                    Long otherId = m.getSender().getId().equals(myId)
                            ? m.getReceiver().getId()
                            : m.getSender().getId();
                    String otherName = m.getSender().getId().equals(myId)
                            ? m.getReceiver().getFullName()
                            : m.getSender().getFullName();
                    t.put("otherUserId", otherId);
                    t.put("otherUserName", otherName);
                    return t;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Chat threads", threads));
    }

    private Map<String, Object> toMap(ChatMessage m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("senderId", m.getSender().getId());
        map.put("senderName", m.getSender().getFullName());
        map.put("receiverId", m.getReceiver().getId());
        map.put("propertyId", m.getProperty().getId());
        map.put("propertyTitle", m.getProperty().getTitle());
        map.put("content", m.getContent());
        map.put("isRead", m.getIsRead());
        map.put("sentAt", m.getSentAt());
        return map;
    }

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}