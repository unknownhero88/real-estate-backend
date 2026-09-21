package com.example.demo.service;

import com.example.demo.dto.response.SellerStatsResponse;

public interface SellerService {

    SellerStatsResponse getSellerStats(Long sellerId);
}