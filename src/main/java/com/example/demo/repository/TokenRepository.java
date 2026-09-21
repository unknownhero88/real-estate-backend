package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.enums.TokenType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenAndTokenType(
        String token, TokenType tokenType);

    List<VerificationToken> findAllByUserAndTokenType(
        User user, TokenType tokenType);
}