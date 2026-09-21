package com.example.demo.service;

import com.example.demo.dto.response.PropertyResponse;
import java.util.List;

public interface RecommendationService {

    List<PropertyResponse> getRecommendations(Long userId, int limit);

    void updatePreferences(Long userId);

    List<PropertyResponse> getSimilarProperties(Long propertyId, int limit);
}