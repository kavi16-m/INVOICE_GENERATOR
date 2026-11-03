package com.mycompany.invoiceapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.Desktop;

public class MultiItemInvoiceUI extends JFrame {
    
    private JTextField customerNameField;
    private JComboBox<String> productCombo;
    private JTextField quantityField;
    private JLabel priceLabel;
    private JComboBox<String> paymentMethodCombo;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JTextArea outputArea;
    private JButton downloadButton;
    private JButton cloudButton;
    private String lastGeneratedFile;
    
    private Map<String, Double> readymadePrices = new HashMap<>();
    private List<String> cloudStorage = new ArrayList<>();

    public MultiItemInvoiceUI() {
        initializeProducts();
        initializeUI();
    }
    
    private void initializeProducts() {
        readymadePrices.put("Shirt", 29.99);
        readymadePrices.put("T-Shirt", 19.99);
        readymadePrices.put("Jeans", 49.99);
        readymadePrices.put("Dress", 39.99);
        readymadePrices.put("Jacket", 79.99);
        readymadePrices.put("Shoes", 59.99);
        readymadePrices.put("Cap", 14.99);
        readymadePrices.put("Belt", 24.99);
    }

    private void initializeUI() {
        setTitle("Multi-Item Invoice Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        customerNameField = new JTextField(20);
        inputPanel.add(customerNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Select Product:"), gbc);
        gbc.gridx = 1;
        productCombo = new JComboBox<>(readymadePrices.keySet().toArray(new String[0]));
        productCombo.addActionListener(e -> updatePrice());
        inputPanel.add(productCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField(20);
        inputPanel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1;
        priceLabel = new JLabel("$29.99");
        priceLabel.setBorder(BorderFactory.createEtchedBorder());
        inputPanel.add(priceLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        String[] paymentMethods = {"Credit Card", "Debit Card", "Cash", "UPI"};
        paymentMethodCombo = new JComboBox<>(paymentMethods);
        inputPanel.add(paymentMethodCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JButton addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(new AddItemListener());
        inputPanel.add(addItemButton, gbc);
        
        gbc.gridx = 1; gbc.gridy = 5;
        JPanel itemButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton clearAllButton = new JButton("Clear All");
        clearAllButton.addActionListener(e -> tableModel.setRowCount(0));
        JButton deleteLastButton = new JButton("Delete Last");
        deleteLastButton.addActionListener(e -> {
            if (tableModel.getRowCount() > 0) {
                tableModel.removeRow(tableModel.getRowCount() - 1);
            }
        });
        itemButtonPanel.add(clearAllButton);
        itemButtonPanel.add(deleteLastButton);
        inputPanel.add(itemButtonPanel, gbc);

        String[] columns = {"Product", "Quantity", "Price", "GST%", "Total"};
        tableModel = new DefaultTableModel(columns, 0);
        itemsTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(itemsTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 150));

        // Cloud Account Input
        gbc.gridx = 0; gbc.gridy = 6;
        inputPanel.add(new JLabel("Cloud Account:"), gbc);
        gbc.gridx = 1;
        JTextField cloudAccountField = new JTextField(20);
        cloudAccountField.setText("your-email@gmail.com");
        inputPanel.add(cloudAccountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton generateButton = new JButton("Generate Invoice");
        generateButton.addActionListener(new GenerateButtonListener());
        downloadButton = new JButton("Download");
        downloadButton.addActionListener(new DownloadButtonListener());
        downloadButton.setEnabled(false);
        cloudButton = new JButton("Save to Cloud");
        cloudButton.addActionListener(new CloudButtonListener());
        cloudButton.setEnabled(false);
        JButton viewPDFButton = new JButton("View PDFs");
        viewPDFButton.addActionListener(new ViewPDFListener());
        JButton viewCloudButton = new JButton("View Cloud");
        viewCloudButton.addActionListener(new ViewCloudListener());
        buttonPanel.add(generateButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(cloudButton);
        buttonPanel.add(viewPDFButton);
        buttonPanel.add(viewCloudButton);
        inputPanel.add(buttonPanel, gbc);

        outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tableScrollPane, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void updatePrice() {
        String selectedProduct = (String) productCombo.getSelectedItem();
        if (selectedProduct != null) {
            double price = readymadePrices.get(selectedProduct);
            priceLabel.setText("$" + String.format("%.2f", price));
        }
    }

    private class AddItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String product = (String) productCombo.getSelectedItem();
                String quantityText = quantityField.getText().trim();

                if (quantityText.isEmpty()) {
                    showError("Please enter quantity.");
                    return;
                }

                int quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    showError("Quantity must be positive.");
                    return;
                }

                double price = readymadePrices.get(product);
                double gstRate = 12.0; // All clothing items
                double itemSubtotal = quantity * price;
                double gstAmount = (itemSubtotal * gstRate) / 100;
                double total = itemSubtotal + gstAmount;

                tableModel.addRow(new Object[]{
                    product, quantity, String.format("%.2f", price),
                    String.format("%.1f", gstRate), String.format("%.2f", total)
                });

                quantityField.setText("");

            } catch (NumberFormatException ex) {
                showError("Please enter valid quantity.");
            }
        }
    }

    private class GenerateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String customerName = customerNameField.getText().trim();
                String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

                if (customerName.isEmpty()) {
                    showError("Please enter customer name.");
                    return;
                }

                if (tableModel.getRowCount() == 0) {
                    showError("Please add items to cart.");
                    return;
                }

                double subtotal = 0;
                double totalGST = 0;

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    double itemTotal = Double.parseDouble(tableModel.getValueAt(i, 4).toString());
                    double gstRate = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                    double itemSubtotal = itemTotal / (1 + gstRate / 100);
                    double itemGST = itemTotal - itemSubtotal;

                    subtotal += itemSubtotal;
                    totalGST += itemGST;
                }

                double total = subtotal + totalGST;

                String invoice = generateInvoice(customerName, paymentMethod, subtotal, totalGST, total);

                String filename = "Invoice_" + customerName.replaceAll("[^a-zA-Z0-9._-]", "_") + ".txt";
                saveInvoiceToFile(invoice, filename);
                lastGeneratedFile = filename;

                outputArea.setText(invoice + "\n\nInvoice saved as: " + filename);
                downloadButton.setEnabled(true);

                cloudButton.setEnabled(true);

            } catch (Exception ex) {
                showError("Error generating invoice: " + ex.getMessage());
            }
        }
    }

    private String generateInvoice(String customerName, String paymentMethod, double subtotal, double totalGST, double total) {
        StringBuilder invoice = new StringBuilder();
        invoice.append("=".repeat(60)).append("\n");
        invoice.append("                    INVOICE\n");
        invoice.append("=".repeat(60)).append("\n\n");
        invoice.append("Customer: ").append(customerName).append("\n");
        invoice.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append("\n");
        invoice.append("Payment: ").append(paymentMethod).append("\n\n");

        invoice.append("Items:\n");
        invoice.append("-".repeat(60)).append("\n");

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String product = tableModel.getValueAt(i, 0).toString();
            String quantity = tableModel.getValueAt(i, 1).toString();
            String price = tableModel.getValueAt(i, 2).toString();
            String gst = tableModel.getValueAt(i, 3).toString();
            String itemTotal = tableModel.getValueAt(i, 4).toString();

            invoice.append(String.format("%-15s Qty: %3s  Price: $%8s  GST: %4s%%\n",
                product, quantity, price, gst));
            invoice.append(String.format("%45s Total: $%8s\n", "", itemTotal));
        }

        invoice.append("-".repeat(60)).append("\n");
        invoice.append(String.format("Subtotal: $%8.2f\n", subtotal));
        invoice.append(String.format("Total GST: $%8.2f\n", totalGST));
        invoice.append(String.format("Grand Total: $%8.2f\n", total));
        invoice.append("=".repeat(60)).append("\n");
        return invoice.toString();
    }

    private class DownloadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (lastGeneratedFile != null) {
                try {
                    String pdfFile = lastGeneratedFile.replace(".txt", ".pdf");
                    String downloadsPath = System.getProperty("user.home") + "\\Downloads\\" + pdfFile;
                    
                    // Save invoice as readable text file with .pdf extension
                    try (FileWriter writer = new FileWriter(downloadsPath)) {
                        writer.write(outputArea.getText());
                    }
                    
                    showMessage("Invoice saved to Downloads folder: " + pdfFile + "\nFile can be opened with any text editor.");
                    
                    // Open file manager to show the file
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new java.io.File(System.getProperty("user.home") + "\\Downloads"));
                    }
                    
                } catch (Exception ex) {
                    showError("Error saving invoice: " + ex.getMessage());
                }
            }
        }
    }

    private class CloudButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (lastGeneratedFile != null) {
                try {
                    // Get cloud account from input field
                    JTextField cloudField = (JTextField) ((JPanel) getContentPane().getComponent(0)).getComponent(13);
                    String cloudAccount = cloudField.getText().trim();
                    
                    if (cloudAccount.isEmpty()) {
                        showError("Please enter your cloud account email.");
                        return;
                    }
                    
                    // Save to cloud storage simulation
                    cloudStorage.add("Account: " + cloudAccount + "\n" + outputArea.getText());
                    
                    // Save backup to OneDrive/Google Drive folder if exists
                    String cloudFolder = System.getProperty("user.home") + "\\OneDrive\\Documents\\InvoiceBackups";
                    java.io.File folder = new java.io.File(cloudFolder);
                    if (!folder.exists()) {
                        cloudFolder = System.getProperty("user.home") + "\\Google Drive\\InvoiceBackups";
                        folder = new java.io.File(cloudFolder);
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                    }
                    
                    String backupFile = cloudFolder + "\\Invoice_" + cloudAccount.replaceAll("[^a-zA-Z0-9._-]", "_") + 
                                       "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
                    
                    try (FileWriter writer = new FileWriter(backupFile)) {
                        writer.write("CLOUD BACKUP\n");
                        writer.write("Account: " + cloudAccount + "\n");
                        writer.write("Backup Time: " + LocalDateTime.now() + "\n\n");
                        writer.write(outputArea.getText());
                    }
                    
                    showMessage("Invoice saved to cloud account: " + cloudAccount + 
                               "\nBackup saved to: " + backupFile + 
                               "\nTotal cloud invoices: " + cloudStorage.size());
                    
                } catch (Exception ex) {
                    showError("Error saving to cloud: " + ex.getMessage());
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

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private class ViewPDFListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                java.io.File currentDir = new java.io.File(".");
                java.io.File[] pdfFiles = currentDir.listFiles((dir, name) -> name.endsWith("_PDF.txt"));
                
                if (pdfFiles == null || pdfFiles.length == 0) {
                    showMessage("No PDF files found. Generate and download an invoice first.");
                    return;
                }
                
                StringBuilder fileList = new StringBuilder("Downloaded PDF Files:\n\n");
                for (java.io.File file : pdfFiles) {
                    fileList.append("• ").append(file.getName()).append("\n");
                }
                
                JTextArea textArea = new JTextArea(fileList.toString());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                
                JOptionPane.showMessageDialog(MultiItemInvoiceUI.this, scrollPane, "Downloaded PDFs", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception ex) {
                showError("Error viewing PDF files: " + ex.getMessage());
            }
        }
    }
    
    private class ViewCloudListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (cloudStorage.isEmpty()) {
                showMessage("No invoices in cloud storage. Save an invoice to cloud first.");
                return;
            }
            
            StringBuilder cloudData = new StringBuilder("Cloud Storage (" + cloudStorage.size() + " invoices):\n\n");
            for (int i = 0; i < cloudStorage.size(); i++) {
                cloudData.append("=== Invoice #").append(i + 1).append(" ===").append("\n");
                String invoice = cloudStorage.get(i);
                String[] lines = invoice.split("\n");
                for (int j = 0; j < Math.min(5, lines.length); j++) {
                    cloudData.append(lines[j]).append("\n");
                }
                cloudData.append("...\n\n");
            }
            
            JTextArea textArea = new JTextArea(cloudData.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));
            
            JOptionPane.showMessageDialog(MultiItemInvoiceUI.this, scrollPane, "Cloud Storage", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MultiItemInvoiceUI().setVisible(true);
        });
    }
}