package com.example.demo.repository;

import com.example.demo.entity.Property;
import com.example.demo.enums.PropertyType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends
        JpaRepository<Property, Long>,
        JpaSpecificationExecutor<Property> {

    // All approved + active listings (for public)
    Page<Property> findByIsApprovedTrueAndIsActiveTrue(Pageable pageable);

    // Seller's own listings
    Page<Property> findBySellerId(Long sellerId, Pageable pageable);
    List<Property> findBySellerId(Long sellerId);

    // Search by city
    Page<Property> findByIsApprovedTrueAndIsActiveTrueAndCityContainingIgnoreCase(
            String city, Pageable pageable);

    // Count by seller
    long countBySellerId(Long sellerId);

    // Increment view count
    @Modifying
    @Query("UPDATE Property p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);


    // ==================== ADD THESE METHODS ====================

    // Admin: Get ALL properties (including unapproved)
    Page<Property> findAll(Pageable pageable);

    // Admin: Get pending approval properties
    Page<Property> findByIsApprovedFalseAndIsActiveTrue(Pageable pageable);

    // Count statistics
    long countByIsApprovedTrue();
    long countByIsApprovedFalseAndIsActiveTrue();

    // Admin: Approve property
    @Modifying
    @Query("UPDATE Property p SET p.isApproved = true WHERE p.id = :id")
    void approveProperty(@Param("id") Long id);

    // Admin: Reject property (soft delete)
    @Modifying
    @Query("UPDATE Property p SET p.isActive = false WHERE p.id = :id")
    void rejectProperty(@Param("id") Long id);


    // Add these methods:

    long countByType(PropertyType type);

    @Query("SELECT p.city, COUNT(p) FROM Property p " +
            "WHERE p.isApproved = true AND p.isActive = true " +
            "GROUP BY p.city ORDER BY COUNT(p) DESC")
    List<Object[]> findTopCities();

    @Query("SELECT p.title, p.viewCount, " +
            "(SELECT COUNT(i) FROM Inquiry i WHERE i.property = p) " +
            "FROM Property p WHERE p.seller.id = :sellerId " +
            "ORDER BY p.viewCount DESC")
    List<Object[]> findSellerListingStats(@Param("sellerId") Long sellerId);
}