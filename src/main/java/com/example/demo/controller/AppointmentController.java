package com.example.demo.controller;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.entity.Property;
import com.example.demo.entity.User;
import com.example.demo.enums.AppointmentStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.PropertyRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                 PropertyRepository propertyRepository,
                                 UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    // Buyer: Book an appointment
    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> bookAppointment(
            @RequestParam Long propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentDate,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserDetails ud) {

        User buyer = getUser(ud);
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        Appointment appointment = new Appointment();
        appointment.setProperty(property);
        appointment.setBuyer(buyer);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setNote(note);

        appointmentRepository.save(appointment);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Appointment booked successfully", toMap(appointment))
        );
    }

    // Buyer: Get my appointments
    @GetMapping("/my-appointments")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> myAppointments(
            @AuthenticationPrincipal UserDetails ud) {

        Long buyerId = getUser(ud).getId();
        List<Appointment> appointments = appointmentRepository
                .findByBuyerIdOrderByAppointmentDateAsc(buyerId);

        return ResponseEntity.ok(ApiResponse.success(
                "My appointments",
                appointments.stream().map(this::toMap).collect(Collectors.toList())
        ));
    }

    // Seller: Get incoming appointments
    @GetMapping("/incoming")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> incomingAppointments(
            @AuthenticationPrincipal UserDetails ud) {

        Long sellerId = getUser(ud).getId();
        List<Appointment> appointments = appointmentRepository
                .findByPropertySellerIdOrderByAppointmentDateAsc(sellerId);

        return ResponseEntity.ok(ApiResponse.success(
                "Incoming appointments",
                appointments.stream().map(this::toMap).collect(Collectors.toList())
        ));
    }

    // Seller: Update appointment status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status,
            @AuthenticationPrincipal UserDetails ud) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        appointment.setStatus(status);
        appointmentRepository.save(appointment);

        return ResponseEntity.ok(ApiResponse.success("Appointment status updated", null));
    }

    private Map<String, Object> toMap(Appointment a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("propertyId", a.getProperty().getId());
        m.put("propertyTitle", a.getProperty().getTitle());
        m.put("buyerName", a.getBuyer().getFullName());
        m.put("appointmentDate", a.getAppointmentDate());
        m.put("note", a.getNote());
        m.put("status", a.getStatus().name());
        m.put("createdAt", a.getCreatedAt());
        return m;
    }

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}