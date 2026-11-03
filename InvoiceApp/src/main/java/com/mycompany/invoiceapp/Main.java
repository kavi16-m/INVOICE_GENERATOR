package com.mycompany.invoiceapp;

import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    
    public static void main(String[] args) {
        InvoiceGenerator generator = new InvoiceGenerator();
        
        try {
            generator.generateInvoice("John Doe", "Laptop", 2, 999.99, "Express delivery");
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Invalid input provided", e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed to generate invoice", e);
        }
    }
}