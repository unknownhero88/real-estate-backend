package com.example.demo.service;

import com.example.demo.dto.request.InquiryRequest;
import com.example.demo.dto.response.InquiryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InquiryService {

    InquiryResponse sendInquiry(InquiryRequest request, Long buyerId);

    Page<InquiryResponse> getBuyerInquiries(Long buyerId, Pageable pageable);

    Page<InquiryResponse> getSellerInquiries(Long sellerId, Pageable pageable);

    void closeInquiry(Long inquiryId, Long sellerId);
}