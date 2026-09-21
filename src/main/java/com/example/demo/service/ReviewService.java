package com.example.demo.service;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse addReview(ReviewRequest request, Long userId);

    void deleteReview(Long reviewId, Long userId);

    Page<ReviewResponse> getPropertyReviews(Long propertyId, Pageable pageable);

    Double getAverageRating(Long propertyId);
}