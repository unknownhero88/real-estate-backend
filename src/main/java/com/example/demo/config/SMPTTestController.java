package com.example.demo.config;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SMPTTestController {

	@GetMapping("/api/test/smtp")
	public ResponseEntity<String> testSmtp() {

	    try (Socket socket = new Socket()) {

	        socket.connect(
	            new InetSocketAddress("smtp.resend.com", 2587),
	            10000
	        );

	        return ResponseEntity.ok("SMTP connection successful");

	    } catch (Exception e) {
	        return ResponseEntity
	                .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("SMTP connection failed: " + e.getMessage());
	    }
	}
}
