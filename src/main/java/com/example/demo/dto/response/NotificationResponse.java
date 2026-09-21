package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private Long referenceId;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private NotificationResponse r = new NotificationResponse();

        public Builder id(Long v) { r.id = v; return this; }
        public Builder title(String v) { r.title = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder type(String v) { r.type = v; return this; }
        public Builder isRead(Boolean v) { r.isRead = v; return this; }
        public Builder referenceId(Long v) { r.referenceId = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }

        public NotificationResponse build() { return r; }
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public Boolean getIsRead() { return isRead; }
    public Long getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}