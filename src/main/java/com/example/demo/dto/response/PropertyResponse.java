package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PropertyResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String type;
    private String status;
    private String listingType;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal areaSqft;
    private String city;
    private String state;
    private String address;
    private Boolean isApproved;
    private Long viewCount;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private Long sellerId;
    private String sellerName;
    private String sellerEmail;
    private String sellerPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Builder Pattern
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private PropertyResponse r = new PropertyResponse();

        public Builder id(Long v) { r.id = v; return this; }
        public Builder title(String v) { r.title = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder price(BigDecimal v) { r.price = v; return this; }
        public Builder type(String v) { r.type = v; return this; }
        public Builder status(String v) { r.status = v; return this; }
        public Builder listingType(String v) { r.listingType = v; return this; }
        public Builder bedrooms(Integer v) { r.bedrooms = v; return this; }
        public Builder bathrooms(Integer v) { r.bathrooms = v; return this; }
        public Builder areaSqft(BigDecimal v) { r.areaSqft = v; return this; }
        public Builder city(String v) { r.city = v; return this; }
        public Builder state(String v) { r.state = v; return this; }
        public Builder address(String v) { r.address = v; return this; }
        public Builder isApproved(Boolean v) { r.isApproved = v; return this; }
        public Builder viewCount(Long v) { r.viewCount = v; return this; }
        public Builder imageUrls(List<String> v) { r.imageUrls = v; return this; }
        public Builder primaryImageUrl(String v) { r.primaryImageUrl = v; return this; }
        public Builder sellerId(Long v) { r.sellerId = v; return this; }
        public Builder sellerName(String v) { r.sellerName = v; return this; }
        public Builder sellerEmail(String v) { r.sellerEmail = v; return this; }
        public Builder sellerPhone(String v) { r.sellerPhone = v; return this; }
        public Builder createdAt(LocalDateTime v) { r.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { r.updatedAt = v; return this; }

        public PropertyResponse build() { return r; }
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getListingType() { return listingType; }
    public Integer getBedrooms() { return bedrooms; }
    public Integer getBathrooms() { return bathrooms; }
    public BigDecimal getAreaSqft() { return areaSqft; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getAddress() { return address; }
    public Boolean getIsApproved() { return isApproved; }
    public Long getViewCount() { return viewCount; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public Long getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getSellerEmail() { return sellerEmail; }
    public String getSellerPhone() { return sellerPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}