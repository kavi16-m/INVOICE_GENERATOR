package com.mycompany.invoiceapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.logging.Level;

public class InvoiceUI extends JFrame {
    private static final Logger logger = Logger.getLogger(InvoiceUI.class.getName());
    
    private JTextField customerNameField;
    private JTextField productNameField;
    private JTextField quantityField;
    private JTextField priceField;
    private JTextField extraField;
    private JTextArea outputArea;
    private InvoiceGenerator generator;

    public InvoiceUI() {
        generator = new InvoiceGenerator();
        initializeUI();
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

        // Extra Field
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Extra Info:"), gbc);
        gbc.gridx = 1;
        extraField = new JTextField(20);
        inputPanel.add(extraField, gbc);

        // Generate Button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton generateButton = new JButton("Generate Invoice");
        generateButton.addActionListener(new GenerateButtonListener());
        inputPanel.add(generateButton, gbc);

        // Output Area
        outputArea = new JTextArea(10, 40);
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
                String extraInfo = extraField.getText().trim();

                if (customerName.isEmpty() || productName.isEmpty() || 
                    quantityText.isEmpty() || priceText.isEmpty()) {
                    showError("Please fill in all required fields.");
                    return;
                }

                int quantity = Integer.parseInt(quantityText);
                double price = Double.parseDouble(priceText);

                generator.generateInvoice(customerName, productName, quantity, price, extraInfo);
                
                double total = quantity * price;
                String output = String.format(
                    "Invoice Generated Successfully!\n\n" +
                    "Customer: %s\n" +
                    "Product: %s\n" +
                    "Quantity: %d\n" +
                    "Price per unit: $%.2f\n" +
                    "Extra Info: %s\n" +
                    "Total Amount: $%.2f\n\n" +
                    "PDF saved as: Invoice_%s.pdf",
                    customerName, productName, quantity, price, 
                    extraInfo.isEmpty() ? "None" : extraInfo, total,
                    customerName.replaceAll("[^a-zA-Z0-9._-]", "_")
                );
                
                outputArea.setText(output);
                
            } catch (NumberFormatException ex) {
                showError("Please enter valid numbers for quantity and price.");
            } catch (IllegalArgumentException ex) {
                showError("Input Error: " + ex.getMessage());
            } catch (Exception ex) {
                showError("Error generating invoice: " + ex.getMessage());
                logger.log(Level.SEVERE, "Unexpected error", ex);
            }
        }
    }

    private void showError(String message) {
        outputArea.setText("ERROR: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel
            }
            new InvoiceUI().setVisible(true);
        });
    }
}