package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByPropertyId(Long propertyId, Pageable pageable);

    Optional<Review> findByPropertyIdAndUserId(Long propertyId, Long userId);

    boolean existsByPropertyIdAndUserId(Long propertyId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.property.id = :propertyId")
    Double findAvgRatingByPropertyId(@Param("propertyId") Long propertyId);

    long countByPropertyId(Long propertyId);
}