package com.example.demo.service.impl;

import com.example.demo.dto.request.ReviewRequest;
import com.example.demo.dto.response.ReviewResponse;
import com.example.demo.entity.Property;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             PropertyRepository propertyRepository,
                             UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ReviewResponse addReview(ReviewRequest req, Long userId) {
        if (reviewRepository.existsByPropertyIdAndUserId(req.getPropertyId(), userId)) {
            throw new IllegalStateException("You have already reviewed this property");
        }

        Property property = propertyRepository.findById(req.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Review review = new Review();
        review.setProperty(property);
        review.setUser(user);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        reviewRepository.save(review);
        log.info("Review added by user {} for property {}", userId, req.getPropertyId());

        return toResponse(review);
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new SecurityException("Not your review");
        }

        reviewRepository.delete(review);
    }

    @Override
    public Page<ReviewResponse> getPropertyReviews(Long propertyId, Pageable pageable) {
        return reviewRepository.findByPropertyId(propertyId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Double getAverageRating(Long propertyId) {
        Double avg = reviewRepository.findAvgRatingByPropertyId(propertyId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .propertyId(r.getProperty().getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getFullName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}