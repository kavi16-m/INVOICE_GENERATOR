package com.mycompany.invoiceapp;

public class App {
    public static void main(String[] args) {
        // Create an object of InvoiceGenerator
        InvoiceGenerator generator = new InvoiceGenerator();

        // Call the method on the object
        generator.generateInvoice(
            "John Doe",        // customerName
            "Laptop",          // productName
            2,                 // quantity
            55000.0,           // price
            "Paid via UPI"     // extraField
        );
    }
}
