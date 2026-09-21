package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class InquiryResponse {

    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private Long buyerId;
    private String buyerName;
    private String buyerEmail;
    private String message;
    private String status;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private InquiryResponse r = new InquiryResponse();

        public Builder id(Long v) { r.id = v; return this; }
        public Builder propertyId(Long v) { r.propertyId = v; return this; }
        public Builder propertyTitle(String v) { r.propertyTitle = v; return this; }
        public Builder buyerId(Long v) { r.buyerId = v; return this; }
        public Builder buyerName(String v) { r.buyerName = v; return this; }
        public Builder buyerEmail(String v) { r.buyerEmail = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder status(String v) { r.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }

        public InquiryResponse build() { return r; }
    }

    // Getters
    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getPropertyTitle() { return propertyTitle; }
    public Long getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public String getBuyerEmail() { return buyerEmail; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}