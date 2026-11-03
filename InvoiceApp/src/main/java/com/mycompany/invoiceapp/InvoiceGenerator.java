package com.mycompany.invoiceapp;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class InvoiceGenerator {
    private static final Logger logger = Logger.getLogger(InvoiceGenerator.class.getName());

    public void generateInvoice(String customerName, String productName, int quantity, double price, String extraField) {
        // Input validation
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be null or empty");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (extraField == null) {
            extraField = "";
        }

        double total = quantity * price;

        // Sanitized logging
        logger.info("Generating invoice for customer");
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format("Product: %s, Quantity: %d, Price: %.2f, Total: %.2f", 
                sanitizeForLog(productName), quantity, price, total));
        }

        // PDF generation with proper resource management
        String dest = "Invoice_" + sanitizeFileName(customerName) + ".pdf";

        try (PdfWriter writer = new PdfWriter(dest);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("Invoice"));
            document.add(new Paragraph("Customer Name: " + customerName));
            document.add(new Paragraph("Product: " + productName));
            document.add(new Paragraph("Quantity: " + quantity));
            document.add(new Paragraph("Price per unit: " + price));
            document.add(new Paragraph("Extra Info: " + extraField));
            document.add(new Paragraph("Total Amount: " + total));

            logger.info("Invoice generated successfully: " + dest);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error generating invoice", e);
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }

    private String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\u0000-\u001F\u007F]", "_");
    }

    private String sanitizeFileName(String input) {
        if (input == null) return "unknown";
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
