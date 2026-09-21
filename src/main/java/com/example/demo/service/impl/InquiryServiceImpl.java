package com.example.demo.service.impl;

import com.example.demo.dto.request.InquiryRequest;
import com.example.demo.dto.response.InquiryResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.InquiryStatus;
import com.example.demo.enums.NotificationType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.InquiryService;
import com.example.demo.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InquiryServiceImpl implements InquiryService {

    private static final Logger log = LoggerFactory.getLogger(InquiryServiceImpl.class);

    private final InquiryRepository inquiryRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public InquiryServiceImpl(InquiryRepository inquiryRepository,
                              PropertyRepository propertyRepository,
                              UserRepository userRepository,
                              NotificationService notificationService) {
        this.inquiryRepository  = inquiryRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository     = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public InquiryResponse sendInquiry(InquiryRequest req, Long buyerId) {
        Property property = propertyRepository.findById(req.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Inquiry inquiry = new Inquiry();
        inquiry.setProperty(property);
        inquiry.setBuyer(buyer);
        inquiry.setMessage(req.getMessage());
        inquiry.setStatus(InquiryStatus.PENDING);

        inquiryRepository.save(inquiry);

        // Notify Seller
        notificationService.createNotification(
                property.getSeller().getId(),
                "New Inquiry Received",
                buyer.getFullName() + " sent an inquiry for: " + property.getTitle(),
                NotificationType.INQUIRY_RECEIVED,
                inquiry.getId()
        );

        log.info("Inquiry sent by buyer {} for property {}", buyerId, property.getId());
        return toResponse(inquiry);
    }

    @Override
    public Page<InquiryResponse> getBuyerInquiries(Long buyerId, Pageable pageable) {
        return inquiryRepository.findByBuyerId(buyerId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<InquiryResponse> getSellerInquiries(Long sellerId, Pageable pageable) {
        return inquiryRepository.findByPropertySellerId(sellerId, pageable)
                .map(this::toResponse);
    }

    @Override
    public void closeInquiry(Long inquiryId, Long sellerId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));

        if (!inquiry.getProperty().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("Not your inquiry");
        }

        inquiry.setStatus(InquiryStatus.CLOSED);
        inquiryRepository.save(inquiry);
    }

    private InquiryResponse toResponse(Inquiry i) {
        return InquiryResponse.builder()
                .id(i.getId())
                .propertyId(i.getProperty().getId())
                .propertyTitle(i.getProperty().getTitle())
                .buyerId(i.getBuyer().getId())
                .buyerName(i.getBuyer().getFullName())
                .buyerEmail(i.getBuyer().getEmail())
                .message(i.getMessage())
                .status(i.getStatus().name())
                .createdAt(i.getCreatedAt())
                .build();
    }
}