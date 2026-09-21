package com.example.demo.service.impl;

import com.example.demo.dto.request.PropertyRequest;
import com.example.demo.dto.response.PropertyResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.ListingType;
import com.example.demo.enums.PropertyStatus;
import com.example.demo.enums.PropertyType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import com.example.demo.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PropertyServiceImpl implements PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository imageRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/properties}")
    private String uploadDir;

    @Value("${app.base.url:http://localhost:8088}")
    private String baseUrl;

    public PropertyServiceImpl(PropertyRepository propertyRepository,
                               PropertyImageRepository imageRepository,
                               UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.imageRepository    = imageRepository;
        this.userRepository     = userRepository;
    }

    @Override
    public PropertyResponse createProperty(PropertyRequest req, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Property p = new Property();
        mapRequestToEntity(req, p);
        p.setSeller(seller);
        p.setStatus(PropertyStatus.AVAILABLE);
        p.setIsApproved(false); // Admin approval required

        propertyRepository.save(p);
        log.info("Property created: {} by seller: {}", p.getId(), sellerId);
        return toResponse(p);
    }

    @Override
    public PropertyResponse updateProperty(Long id, PropertyRequest req, Long sellerId) {
        Property p = getOwnedProperty(id, sellerId);
        mapRequestToEntity(req, p);
        propertyRepository.save(p);
        log.info("Property updated: {}", id);
        return toResponse(p);
    }

    @Override
    public void deleteProperty(Long id, Long sellerId) {
        Property p = getOwnedProperty(id, sellerId);
        p.setIsActive(false); // Soft delete
        propertyRepository.save(p);
        log.info("Property soft-deleted: {}", id);
    }

    @Override
    @Transactional
    public PropertyResponse getPropertyById(Long id) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        propertyRepository.incrementViewCount(id);
        return toResponse(p);
    }

    @Override
    public Page<PropertyResponse> getAllProperties(Pageable pageable) {
        return propertyRepository
                .findByIsApprovedTrueAndIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<PropertyResponse> getSellerProperties(Long sellerId, Pageable pageable) {
        return propertyRepository.findBySellerId(sellerId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<PropertyResponse> searchProperties(
            String city, String type, String listingType,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer bedrooms, Pageable pageable) {

        Specification<Property> spec = Specification
                .where(isApprovedAndActive())
                .and(cityContains(city))
                .and(hasType(type))
                .and(hasListingType(listingType))
                .and(priceBetween(minPrice, maxPrice))
                .and(hasBedrooms(bedrooms));

        return propertyRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    public List<String> uploadImages(Long propertyId, List<MultipartFile> files, Long sellerId) {
        Property p = getOwnedProperty(propertyId, sellerId);
        List<String> urls = new ArrayList<>();
        boolean firstImage = p.getImages().isEmpty();

        for (MultipartFile file : files) {
            String url = saveFile(file);
            PropertyImage img = new PropertyImage(p, url, firstImage);
            imageRepository.save(img);
            urls.add(url);
            firstImage = false;
        }
        return urls;
    }

    @Override
    public void deleteImage(Long imageId, Long sellerId) {
        PropertyImage img = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        if (!img.getProperty().getSeller().getId().equals(sellerId)) {
            throw new SecurityException("Not your image");
        }
        imageRepository.delete(img);
    }

    // ==================== PRIVATE HELPERS ====================

    private Property getOwnedProperty(Long id, Long sellerId) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (!p.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("You don't own this property");
        }
        return p;
    }

    private void mapRequestToEntity(PropertyRequest req, Property p) {
        p.setTitle(req.getTitle());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setType(req.getType());
        p.setListingType(req.getListingType());
        p.setBedrooms(req.getBedrooms());
        p.setBathrooms(req.getBathrooms());
        p.setAreaSqft(req.getAreaSqft());
        p.setCity(req.getCity());
        p.setState(req.getState());
        p.setAddress(req.getAddress());
    }

    private String saveFile(MultipartFile file) {
        try {
            Path dirPath = Paths.get(uploadDir);
            Files.createDirectories(dirPath);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            return baseUrl + "/uploads/properties/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage());
        }
    }

    private PropertyResponse toResponse(Property p) {
        List<String> urls = p.getImages().stream()
                .map(PropertyImage::getImageUrl).collect(Collectors.toList());

        String primary = p.getImages().stream()
                .filter(i -> Boolean.TRUE.equals(i.getIsPrimary()))
                .map(PropertyImage::getImageUrl)
                .findFirst().orElse(null);

        return PropertyResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .price(p.getPrice())
                .type(p.getType() != null ? p.getType().name() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .listingType(p.getListingType() != null ? p.getListingType().name() : null)
                .bedrooms(p.getBedrooms())
                .bathrooms(p.getBathrooms())
                .areaSqft(p.getAreaSqft())
                .city(p.getCity())
                .state(p.getState())
                .address(p.getAddress())
                .isApproved(p.getIsApproved())
                .viewCount(p.getViewCount())
                .imageUrls(urls)
                .primaryImageUrl(primary)
                .sellerId(p.getSeller().getId())
                .sellerName(p.getSeller().getFullName())
                .sellerEmail(p.getSeller().getEmail())
                .sellerPhone(p.getSeller().getPhone())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    public PropertyResponse toResponsePublic(Property property) {
        return toResponse(property);   
    }

    // JPA Specifications
    private Specification<Property> isApprovedAndActive() {
        return (root, q, cb) -> cb.and(
                cb.isTrue(root.get("isApproved")),
                cb.isTrue(root.get("isActive")));
    }

    private Specification<Property> cityContains(String city) {
        return (root, q, cb) -> city == null || city.isBlank() ? null :
                cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    private Specification<Property> hasType(String type) {
        return (root, q, cb) -> type == null || type.isBlank() ? null :
                cb.equal(root.get("type"), PropertyType.valueOf(type.toUpperCase()));
    }

    private Specification<Property> hasListingType(String lt) {
        return (root, q, cb) -> lt == null || lt.isBlank() ? null :
                cb.equal(root.get("listingType"), ListingType.valueOf(lt.toUpperCase()));
    }

    private Specification<Property> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, q, cb) -> {
            if (min != null && max != null)
                return cb.between(root.get("price"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            if (max != null) return cb.lessThanOrEqualTo(root.get("price"), max);
            return null;
        };
    }

    private Specification<Property> hasBedrooms(Integer bedrooms) {
        return (root, q, cb) -> bedrooms == null ? null :
                cb.equal(root.get("bedrooms"), bedrooms);
    }
}