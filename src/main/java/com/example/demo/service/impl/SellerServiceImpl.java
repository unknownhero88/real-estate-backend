package com.example.demo.service.impl;

import com.example.demo.dto.response.SellerStatsResponse;
import com.example.demo.entity.Property;
import com.example.demo.repository.InquiryRepository;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.service.SellerService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerServiceImpl implements SellerService {

    private final PropertyRepository propertyRepository;
    private final InquiryRepository inquiryRepository;

    public SellerServiceImpl(PropertyRepository propertyRepository,
                             InquiryRepository inquiryRepository) {
        this.propertyRepository = propertyRepository;
        this.inquiryRepository = inquiryRepository;
    }

    @Override
    public SellerStatsResponse getSellerStats(Long sellerId) {
        List<Property> myProperties = propertyRepository.findBySellerId(sellerId);

        long totalListings = myProperties.size();
        long approvedListings = myProperties.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsApproved()))
                .count();
        long pendingListings = myProperties.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getIsApproved())
                        && Boolean.TRUE.equals(p.getIsActive()))
                .count();
        long totalViews = myProperties.stream()
                .mapToLong(p -> p.getViewCount() != null ? p.getViewCount() : 0L)
                .sum();

        // Get total inquiries for this seller
        long totalInquiries = inquiryRepository
                .findByPropertySellerId(sellerId, Pageable.unpaged())
                .getTotalElements();

        return SellerStatsResponse.builder()
                .totalListings(totalListings)
                .approvedListings(approvedListings)
                .pendingListings(pendingListings)
                .totalViews(totalViews)
                .totalInquiries(totalInquiries)
                .build();
    }
}