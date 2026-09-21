package com.example.demo.dto.response;

import java.time.LocalDateTime;

public class ReviewResponse {

    private Long id;
    private Long propertyId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ReviewResponse r = new ReviewResponse();

        public Builder id(Long v) { r.id = v; return this; }
        public Builder propertyId(Long v) { r.propertyId = v; return this; }
        public Builder userId(Long v) { r.userId = v; return this; }
        public Builder userName(String v) { r.userName = v; return this; }
        public Builder rating(Integer v) { r.rating = v; return this; }
        public Builder comment(String v) { r.comment = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }

        public ReviewResponse build() { return r; }
    }

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}