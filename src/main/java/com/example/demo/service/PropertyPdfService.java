package com.example.demo.service;

import com.example.demo.entity.Property;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.PropertyRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PropertyPdfService {

    private final PropertyRepository propertyRepository;

    public PropertyPdfService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public byte[] generatePropertyPdf(Long propertyId) {
        Property p = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, new BaseColor(26, 26, 46));
            Font headingFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(15, 52, 96));
            Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.DARK_GRAY);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.GRAY);
            Font priceFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(22, 163, 74));

            // Header
            Paragraph header = new Paragraph("RealEstate Portal - Property Report",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE));
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell(header);
            headerCell.setBackgroundColor(new BaseColor(22, 163, 74));
            headerCell.setPadding(12);
            headerCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(headerCell);
            document.add(headerTable);
            document.add(Chunk.NEWLINE);

            // Title & Location
            document.add(new Paragraph(p.getTitle(), titleFont));
            document.add(new Paragraph(p.getAddress() + ", " + p.getCity() + ", " + p.getState(), bodyFont));
            document.add(Chunk.NEWLINE);

            // Price
            String priceText = "₹ " + String.format("%,.0f", p.getPrice().doubleValue()) +
                    (p.getListingType() != null && p.getListingType().name().equals("RENT") ? " / month" : "");
            document.add(new Paragraph(priceText, priceFont));
            document.add(Chunk.NEWLINE);

            // Details Table
            document.add(new Paragraph("Property Details", headingFont));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{35, 65});

            String[][] details = {
                    {"Type", p.getType() != null ? p.getType().name() : "-"},
                    {"Listing", p.getListingType() != null ? p.getListingType().name() : "-"},
                    {"Bedrooms", p.getBedrooms() != null ? p.getBedrooms().toString() : "-"},
                    {"Bathrooms", p.getBathrooms() != null ? p.getBathrooms().toString() : "-"},
                    {"Area (sqft)", p.getAreaSqft() != null ? p.getAreaSqft().toString() : "-"},
                    {"Status", p.getStatus() != null ? p.getStatus().name() : "-"},
                    {"Views", String.valueOf(p.getViewCount())}
            };

            for (String[] row : details) {
                PdfPCell labelCell = new PdfPCell(new Phrase(row[0], labelFont));
                labelCell.setBackgroundColor(new BaseColor(245, 245, 245));
                labelCell.setPadding(8);
                table.addCell(labelCell);

                PdfPCell valueCell = new PdfPCell(new Phrase(row[1], bodyFont));
                valueCell.setPadding(8);
                table.addCell(valueCell);
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            // Description
            document.add(new Paragraph("Description", headingFont));
            document.add(new Paragraph(p.getDescription() != null ? p.getDescription() : "No description available.", bodyFont));
            document.add(Chunk.NEWLINE);

            // Seller Info
            document.add(new Paragraph("Seller Information", headingFont));
            document.add(new Paragraph("Name: " + p.getSeller().getFullName(), bodyFont));
            document.add(new Paragraph("Email: " + p.getSeller().getEmail(), bodyFont));
            if (p.getSeller().getPhone() != null) {
                document.add(new Paragraph("Phone: " + p.getSeller().getPhone(), bodyFont));
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage());
        }
    }
}