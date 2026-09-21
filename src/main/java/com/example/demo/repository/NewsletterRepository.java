package com.example.demo.repository;

import com.example.demo.entity.Newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterRepository extends JpaRepository<Newsletter, Long> {

    boolean existsByEmail(String email);

    Optional<Newsletter> findByEmail(String email);

    List<Newsletter> findByIsActiveTrue();

    long countByIsActiveTrue();
}