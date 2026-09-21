package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Agent;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentRepository agentRepository;
    private final UserRepository userRepository;

    public AgentController(AgentRepository agentRepository, UserRepository userRepository) {
        this.agentRepository = agentRepository;
        this.userRepository = userRepository;
    }

    public static class AgentRequest {
        public String agencyName;
        public String licenseNumber;
        public String bio;
        public Integer experienceYears;
        public String specialization;
    }

    // Public: Get all verified agents
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("totalDeals").descending());
        Page<Agent> agents = agentRepository.findByIsVerifiedTrue(pageable);

        return ResponseEntity.ok(ApiResponse.success("Verified Agents",
                agents.map(this::toMap)));
    }

    // Public: Get single agent
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOne(@PathVariable Long id) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        return ResponseEntity.ok(ApiResponse.success("Agent details", toMap(agent)));
    }

    // Seller: Create/Update own Agent Profile
    @PostMapping("/my-profile")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> upsert(
            @RequestBody AgentRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Agent agent = agentRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Agent newAgent = new Agent();
                    newAgent.setUser(user);
                    return newAgent;
                });

        if (req.agencyName != null) agent.setAgencyName(req.agencyName);
        if (req.licenseNumber != null) agent.setLicenseNumber(req.licenseNumber);
        if (req.bio != null) agent.setBio(req.bio);
        if (req.experienceYears != null) agent.setExperienceYears(req.experienceYears);
        if (req.specialization != null) agent.setSpecialization(req.specialization);

        agentRepository.save(agent);

        return ResponseEntity.ok(ApiResponse.success("Agent profile updated", toMap(agent)));
    }

    private Map<String, Object> toMap(Agent a) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", a.getId());
        map.put("userId", a.getUser().getId());
        map.put("fullName", a.getUser().getFullName());
        map.put("email", a.getUser().getEmail());
        map.put("agencyName", a.getAgencyName());
        map.put("licenseNumber", a.getLicenseNumber());
        map.put("bio", a.getBio());
        map.put("experienceYears", a.getExperienceYears());
        map.put("specialization", a.getSpecialization());
        map.put("isVerified", a.getIsVerified());
        map.put("totalDeals", a.getTotalDeals());
        return map;
    }
}