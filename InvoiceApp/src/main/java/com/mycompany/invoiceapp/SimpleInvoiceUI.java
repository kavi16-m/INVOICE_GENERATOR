package com.mycompany.invoiceapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.awt.Desktop;
import java.io.File;

public class SimpleInvoiceUI extends JFrame {
    
    private JTextField customerNameField;
    private JTextField productNameField;
    private JTextField quantityField;
    private JTextField priceField;
    private JLabel gstLabel;
    private JComboBox<String> paymentMethodCombo;
    private JTextArea outputArea;
    private JButton downloadButton;
    private String lastGeneratedFile;
    
    // GST storage by product category
    private Map<String, Double> gstRates = new HashMap<>();

    public SimpleInvoiceUI() {
        initializeGSTRates();
        initializeUI();
    }
    
    private void initializeGSTRates() {
        gstRates.put("Electronics", 18.0);
        gstRates.put("Clothing", 12.0);
        gstRates.put("Food", 5.0);
        gstRates.put("Books", 0.0);
        gstRates.put("Medicine", 5.0);
        gstRates.put("Services", 18.0);
    }

    private void initializeUI() {
        setTitle("Invoice Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Customer Name
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        customerNameField = new JTextField(20);
        inputPanel.add(customerNameField, gbc);

        // Product Name
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        productNameField = new JTextField(20);
        productNameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String product = productNameField.getText().trim();
                if (!product.isEmpty()) {
                    getGSTRate(product);
                }
            }
        });
        inputPanel.add(productNameField, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(20);
        inputPanel.add(quantityField, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Price per unit:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(20);
        inputPanel.add(priceField, gbc);

        // GST Display
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("GST %:"), gbc);
        gbc.gridx = 1;
        gstLabel = new JLabel("18.0% (Auto-detected)");
        gstLabel.setBorder(BorderFactory.createEtchedBorder());
        inputPanel.add(gstLabel, gbc);

        // Payment Method
        gbc.gridx = 0; gbc.gridy = 5;
        inputPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        String[] paymentMethods = {"Credit Card", "Debit Card", "Cash", "UPI"};
        paymentMethodCombo = new JComboBox<>(paymentMethods);
        inputPanel.add(paymentMethodCombo, gbc);

        // Buttons Panel
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateButton = new JButton("Generate Invoice");
        generateButton.addActionListener(new GenerateButtonListener());
        downloadButton = new JButton("Download Invoice");
        downloadButton.addActionListener(new DownloadButtonListener());
        downloadButton.setEnabled(false);
        buttonPanel.add(generateButton);
        buttonPanel.add(downloadButton);
        inputPanel.add(buttonPanel, gbc);

        // Output Area
        outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String customerName = customerNameField.getText().trim();
                String productName = productNameField.getText().trim();
                String quantityText = quantityField.getText().trim();
                String priceText = priceField.getText().trim();
                String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

                if (customerName.isEmpty() || productName.isEmpty() || 
                    quantityText.isEmpty() || priceText.isEmpty()) {
                    showError("Please fill in all required fields.");
                    return;
                }

                int quantity = Integer.parseInt(quantityText);
                double price = Double.parseDouble(priceText);
                double gstRate = getGSTRate(productName);

                if (quantity <= 0) {
                    showError("Quantity must be positive.");
                    return;
                }
                if (price < 0) {
                    showError("Price cannot be negative.");
                    return;
                }


                double subtotal = quantity * price;
                double gstAmount = (subtotal * gstRate) / 100;
                double total = subtotal + gstAmount;
                
                // Generate text invoice
                String invoice = generateTextInvoice(customerName, productName, quantity, price, subtotal, gstRate, gstAmount, total, paymentMethod);
                
                // Save to file
                String filename = "Invoice_" + customerName.replaceAll("[^a-zA-Z0-9._-]", "_") + ".txt";
                saveInvoiceToFile(invoice, filename);
                lastGeneratedFile = filename;
                
                // Display in UI
                outputArea.setText(invoice + "\n\nInvoice saved as: " + filename);
                downloadButton.setEnabled(true);
                
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers for quantity and price.");
            } catch (Exception ex) {
                showError("Error generating invoice: " + ex.getMessage());
            }
        }
    }

    private String generateTextInvoice(String customerName, String productName, int quantity, double price, double subtotal, double gstRate, double gstAmount, double total, String paymentMethod) {
        StringBuilder invoice = new StringBuilder();
        invoice.append("=".repeat(50)).append("\n");
        invoice.append("                 INVOICE\n");
        invoice.append("=".repeat(50)).append("\n\n");
        invoice.append("Customer Name: ").append(customerName).append("\n");
        invoice.append("Product: ").append(productName).append("\n");
        invoice.append("Quantity: ").append(quantity).append("\n");
        invoice.append("Price per unit: $").append(String.format("%.2f", price)).append("\n");
        invoice.append("-".repeat(30)).append("\n");
        invoice.append("Subtotal: $").append(String.format("%.2f", subtotal)).append("\n");
        invoice.append("GST (").append(String.format("%.1f", gstRate)).append("%): $").append(String.format("%.2f", gstAmount)).append("\n");
        invoice.append("-".repeat(30)).append("\n");
        invoice.append("Total Amount: $").append(String.format("%.2f", total)).append("\n");
        invoice.append("Payment Method: ").append(paymentMethod).append("\n");
        invoice.append("=".repeat(50)).append("\n");
        return invoice.toString();
    }
    
    private double getGSTRate(String productName) {
        String product = productName.toLowerCase();
        if (product.contains("laptop") || product.contains("phone") || product.contains("electronics")) {
            gstLabel.setText("18.0% (Electronics)");
            return 18.0;
        } else if (product.contains("shirt") || product.contains("cloth") || product.contains("dress")) {
            gstLabel.setText("12.0% (Clothing)");
            return 12.0;
        } else if (product.contains("food") || product.contains("snack") || product.contains("meal")) {
            gstLabel.setText("5.0% (Food)");
            return 5.0;
        } else if (product.contains("book") || product.contains("magazine")) {
            gstLabel.setText("0.0% (Books)");
            return 0.0;
        } else if (product.contains("medicine") || product.contains("drug")) {
            gstLabel.setText("5.0% (Medicine)");
            return 5.0;
        } else {
            gstLabel.setText("18.0% (Services)");
            return 18.0;
        }
    }
    
    private class DownloadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (lastGeneratedFile != null) {
                try {
                    File file = new File(lastGeneratedFile);
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showError("Cannot open file automatically. File saved as: " + lastGeneratedFile);
                    }
                } catch (IOException ex) {
                    showError("Error opening file: " + ex.getMessage());
                }
            }
        }
    }

    private void saveInvoiceToFile(String invoice, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(invoice);
        }
    }

    private void showError(String message) {
        outputArea.setText("ERROR: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimpleInvoiceUI().setVisible(true);
        });
    }
}