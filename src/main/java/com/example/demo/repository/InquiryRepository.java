package com.example.demo.repository;

import com.example.demo.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByPropertySellerId(Long sellerId, Pageable pageable);

    Page<Inquiry> findByBuyerId(Long buyerId, Pageable pageable);

    Page<Inquiry> findByPropertyId(Long propertyId, Pageable pageable);

    boolean existsByPropertyIdAndBuyerId(Long propertyId, Long buyerId);
}