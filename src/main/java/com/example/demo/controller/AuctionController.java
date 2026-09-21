package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.AuctionStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.*;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AuctionController(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // Public: Active Auctions
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("endTime").ascending());
        Page<Auction> auctions = auctionRepository.findByStatus(AuctionStatus.ACTIVE, pageable);

        return ResponseEntity.ok(ApiResponse.success("Active Auctions",
                auctions.map(this::toMap)));
    }

    // Public: All Auctions
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        Page<Auction> auctions = auctionRepository.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success("All Auctions",
                auctions.map(this::toMap)));
    }

    // Public: Single Auction Details
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOne(@PathVariable Long id) {
        Auction auction = findAuction(id);
        Map<String, Object> data = toMap(auction);

        // Include bid history
        List<Map<String, Object>> bids = bidRepository.findByAuctionIdOrderByAmountDesc(id)
                .stream()
                .map(this::bidToMap)
                .collect(Collectors.toList());
        data.put("bids", bids);

        return ResponseEntity.ok(ApiResponse.success("Auction Details", data));
    }

    // Seller: Create New Auction
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(
            @RequestParam Long propertyId,
            @RequestParam BigDecimal startPrice,
            @RequestParam(required = false) BigDecimal reservePrice,
            @RequestParam(required = false, defaultValue = "50000") BigDecimal minIncrement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @AuthenticationPrincipal UserDetails userDetails) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        if (auctionRepository.findByPropertyId(propertyId).isPresent()) {
            throw new IllegalStateException("Auction already exists for this property");
        }

        Auction auction = new Auction();
        auction.setProperty(property);
        auction.setStartPrice(startPrice);
        auction.setCurrentHighestBid(startPrice);
        auction.setReservePrice(reservePrice);
        auction.setMinIncrement(minIncrement);
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);
        auction.setStatus(LocalDateTime.now().isAfter(startTime) ? AuctionStatus.ACTIVE : AuctionStatus.UPCOMING);

        auctionRepository.save(auction);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Auction created successfully", toMap(auction)));
    }

    // Buyer: Place Bid
    @PostMapping("/{id}/bid")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeBid(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetails userDetails) {

        Auction auction = findAuction(id);
        User bidder = getUser(userDetails);

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
            throw new IllegalStateException("Auction has ended");
        }

        BigDecimal minBid = auction.getCurrentHighestBid().add(auction.getMinIncrement());
        if (amount.compareTo(minBid) < 0) {
            throw new IllegalArgumentException("Bid must be at least " + minBid);
        }

        // Create new bid
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bidRepository.save(bid);

        // Update auction
        auction.setCurrentHighestBid(amount);
        auctionRepository.save(auction);

        // Broadcast via WebSocket - FIXED
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("auctionId", id);
        update.put("newHighestBid", amount);
        update.put("bidderName", bidder.getFullName());
        update.put("bidAt", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/auction/" + id, (Object) update);


        return ResponseEntity.ok(ApiResponse.success("Bid placed successfully", bidToMap(bid)));
    }

    // Helper methods
    private Auction findAuction(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found"));
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Map<String, Object> toMap(Auction a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("propertyId", a.getProperty().getId());
        m.put("propertyTitle", a.getProperty().getTitle());
        m.put("propertyCity", a.getProperty().getCity());
        m.put("startPrice", a.getStartPrice());
        m.put("currentHighestBid", a.getCurrentHighestBid());
        m.put("minIncrement", a.getMinIncrement());
        m.put("startTime", a.getStartTime());
        m.put("endTime", a.getEndTime());
        m.put("status", a.getStatus().name());
        m.put("totalBids", a.getBids().size());
        if (a.getWinner() != null) m.put("winner", a.getWinner().getFullName());
        return m;
    }

    private Map<String, Object> bidToMap(Bid b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("auctionId", b.getAuction().getId());
        m.put("bidderId", b.getBidder().getId());
        m.put("bidderName", b.getBidder().getFullName());
        m.put("amount", b.getAmount());
        m.put("bidAt", b.getBidAt());
        return m;
    }
}