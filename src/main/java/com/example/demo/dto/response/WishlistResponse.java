package com.example.demo.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WishlistResponse {

    private Long id;
    private Long propertyId;
    private String propertyTitle;
    private String propertyCity;
    private String propertyState;
    private String primaryImageUrl;
    private BigDecimal price;
    private String listingType;
    private String type;
    private LocalDateTime savedAt;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private WishlistResponse r = new WishlistResponse();

        public Builder id(Long v) { r.id = v; return this; }
        public Builder propertyId(Long v) { r.propertyId = v; return this; }
        public Builder propertyTitle(String v) { r.propertyTitle = v; return this; }
        public Builder propertyCity(String v) { r.propertyCity = v; return this; }
        public Builder propertyState(String v) { r.propertyState = v; return this; }
        public Builder primaryImageUrl(String v) { r.primaryImageUrl = v; return this; }
        public Builder price(BigDecimal v) { r.price = v; return this; }
        public Builder listingType(String v) { r.listingType = v; return this; }
        public Builder type(String v) { r.type = v; return this; }
        public Builder savedAt(LocalDateTime v) { r.savedAt = v; return this; }

        public WishlistResponse build() { return r; }
    }

    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public String getPropertyTitle() { return propertyTitle; }
    public String getPropertyCity() { return propertyCity; }
    public String getPropertyState() { return propertyState; }
    public String getPrimaryImageUrl() { return primaryImageUrl; }
    public BigDecimal getPrice() { return price; }
    public String getListingType() { return listingType; }
    public String getType() { return type; }
    public LocalDateTime getSavedAt() { return savedAt; }
}