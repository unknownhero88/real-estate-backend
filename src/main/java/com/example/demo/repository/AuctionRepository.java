package com.example.demo.repository;

import com.example.demo.entity.Auction;
import com.example.demo.enums.AuctionStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Page<Auction> findByStatus(AuctionStatus status, Pageable pageable);

    Optional<Auction> findByPropertyId(Long propertyId);

    List<Auction> findByStatusIn(List<AuctionStatus> statuses);
}