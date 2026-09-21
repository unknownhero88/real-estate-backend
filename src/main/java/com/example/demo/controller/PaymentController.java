package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Payment;
import com.example.demo.entity.Property;
import com.example.demo.entity.User;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    public PaymentController(PaymentRepository paymentRepository,
                             UserRepository userRepository,
                             PropertyRepository propertyRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    // Create Razorpay Order
    @PostMapping("/create-order")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @RequestParam Long propertyId,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetails ud) throws Exception {

        User user = getUser(ud);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" + System.currentTimeMillis());

        Order order = client.orders.create(orderRequest);

        // Save payment record
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setProperty(property);
        payment.setRazorpayOrderId(order.get("id"));
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("orderId", order.get("id"));
        response.put("amount", amount);
        response.put("currency", "INR");
        response.put("keyId", keyId);

        return ResponseEntity.ok(ApiResponse.success("Order created successfully", response));
    }

    // Verify Payment
    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @RequestParam String razorpayOrderId,
            @RequestParam String razorpayPaymentId,
            @RequestParam String razorpaySignature,
            @AuthenticationPrincipal UserDetails ud) throws Exception {

        JSONObject attrs = new JSONObject();
        attrs.put("razorpay_order_id", razorpayOrderId);
        attrs.put("razorpay_payment_id", razorpayPaymentId);
        attrs.put("razorpay_signature", razorpaySignature);

        boolean isValid = Utils.verifyPaymentSignature(attrs, keySecret);

        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid payment signature"));
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment record not found"));

        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", null));
    }

    // Get My Payments
    @GetMapping("/my-payments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myPayments(
            @AuthenticationPrincipal UserDetails ud) {

        Long userId = getUser(ud).getId();
        List<Payment> payments = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<Map<String, Object>> result = payments.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("propertyTitle", p.getProperty().getTitle());
            m.put("amount", p.getAmount());
            m.put("status", p.getStatus().name());
            m.put("createdAt", p.getCreatedAt());
            m.put("paidAt", p.getPaidAt());
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("My payments fetched", result));
    }

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}