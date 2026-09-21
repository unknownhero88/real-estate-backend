package com.example.demo.service;

import com.example.demo.dto.request.PropertyRequest;
import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface PropertyService {

    PropertyResponse createProperty(PropertyRequest request, Long sellerId);

    PropertyResponse updateProperty(Long id, PropertyRequest request, Long sellerId);

    void deleteProperty(Long id, Long sellerId);

    PropertyResponse getPropertyById(Long id);

    Page<PropertyResponse> getAllProperties(Pageable pageable);

    Page<PropertyResponse> getSellerProperties(Long sellerId, Pageable pageable);

    Page<PropertyResponse> searchProperties(
            String city,
            String type,
            String listingType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer bedrooms,
            Pageable pageable);

    List<String> uploadImages(Long propertyId, List<MultipartFile> files, Long sellerId);

    void deleteImage(Long imageId, Long sellerId);

    PropertyResponse toResponsePublic(Property property);
}