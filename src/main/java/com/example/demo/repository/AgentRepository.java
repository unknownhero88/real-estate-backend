package com.example.demo.repository;

import com.example.demo.entity.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Page<Agent> findByIsVerifiedTrue(Pageable pageable);

    Optional<Agent> findByUserId(Long userId);
}