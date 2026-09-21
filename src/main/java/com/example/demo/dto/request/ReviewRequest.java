package com.example.demo.dto.request;

import jakarta.validation.constraints.*;

public class ReviewRequest {

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating minimum is 1")
    @Max(value = 5, message = "Rating maximum is 5")
    private Integer rating;

    private String comment;

    public Long getPropertyId() { return propertyId; }
    public void setPropertyId(Long propertyId) { this.propertyId = propertyId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}