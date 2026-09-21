package com.example.demo.repository;

import com.example.demo.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    List<PropertyImage> findByPropertyId(Long propertyId);

    void deleteByPropertyId(Long propertyId);
}