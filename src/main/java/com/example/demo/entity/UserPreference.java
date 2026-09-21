package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.example.demo.enums.ListingType;
import com.example.demo.enums.PropertyType;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private PropertyType preferredType;

    private String preferredCity;

    @Column(name = "min_budget", precision = 12, scale = 2)
    private BigDecimal minBudget;

    @Column(name = "max_budget", precision = 12, scale = 2)
    private BigDecimal maxBudget;

    @Enumerated(EnumType.STRING)
    private ListingType preferredListingType;

    private Integer preferredBedrooms;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public PropertyType getPreferredType() { return preferredType; }
    public void setPreferredType(PropertyType preferredType) { this.preferredType = preferredType; }
    public String getPreferredCity() { return preferredCity; }
    public void setPreferredCity(String preferredCity) { this.preferredCity = preferredCity; }
    public BigDecimal getMinBudget() { return minBudget; }
    public void setMinBudget(BigDecimal minBudget) { this.minBudget = minBudget; }
    public BigDecimal getMaxBudget() { return maxBudget; }
    public void setMaxBudget(BigDecimal maxBudget) { this.maxBudget = maxBudget; }
    public ListingType getPreferredListingType() { return preferredListingType; }
    public void setPreferredListingType(ListingType preferredListingType) { this.preferredListingType = preferredListingType; }
    public Integer getPreferredBedrooms() { return preferredBedrooms; }
    public void setPreferredBedrooms(Integer preferredBedrooms) { this.preferredBedrooms = preferredBedrooms; }
}