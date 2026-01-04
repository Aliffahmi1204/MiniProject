package GUIs;

import Data.Database;
import Entities.StockItem;
import Entities.Supplier;
import Entities.Transaction;
import Exceptions.OutOfStockEx;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class RestaurantGUI extends JFrame {

    private Database db;
    private JTable stockTable;
    private DefaultTableModel model;

    public RestaurantGUI() {

        // ===== Mature Look & Feel =====
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        setTitle("Restaurant Stock System");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initDatabase();
        initUI();

        setVisible(true);
    }

    private void initDatabase() {
        try {
            db = new Database();
            Connection conn = db.connect();
            db.loadAll();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Database Error:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== Header (Professional) =====
        JLabel title = new JLabel("Restaurant Stock Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(240, 242, 245));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)));
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // ===== Table =====
        model = new DefaultTableModel(
                new String[]{"ID", "Item", "Qty", "Category", "Supplier"}, 0
        );

        stockTable = new JTable(model);
        styleTable(stockTable);
        loadStock();

        add(new JScrollPane(stockTable), BorderLayout.CENTER);

        // ===== Footer Buttons =====
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panel.setBackground(new Color(240, 242, 245));

        JButton addnewBtn = createButton("Add New Stock");
        JButton addBtn = createButton("Add Stock");
        JButton reduceBtn = createButton("Reduce Stock");
        JButton deleteBtn = createButton("Delete Stock");
        JButton refreshBtn = createButton("Refresh");
        JButton transactionBtn = createButton("Transactions");

        panel.add(addnewBtn);
        panel.add(addBtn);
        panel.add(reduceBtn);
        panel.add(deleteBtn);
        panel.add(refreshBtn);
        panel.add(transactionBtn);

        add(panel, BorderLayout.SOUTH);

        addnewBtn.addActionListener(e -> addNewStock());
        addBtn.addActionListener(e -> addStock());
        reduceBtn.addActionListener(e -> reduceStock());
        deleteBtn.addActionListener(e -> deleteStock());
        refreshBtn.addActionListener(e -> loadStock());
        transactionBtn.addActionListener(e -> showTransactions());
    }

    // ===== UI Helpers (ONLY visual) =====
    private void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(220, 230, 245));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        return btn;
    }

    // =========================
    // ===== ORIGINAL LOGIC =====
    // =========================

    private void loadStock() {
        try {
            db.loadAll();
        } catch (Exception e) {
            e.notify();
        }
        model.setRowCount(0);
        for (StockItem s : db.getStockItems()) {
            model.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getQuantity(),
                    s.getCategory(),
                    s.getSupplier().getName()
            });
        }
    }

    private void addNewStock() {
        List<Supplier> suppliers = db.getSuppliers();
        JComboBox<String> supCombo = new JComboBox<>(
                suppliers.stream().map(Supplier::getName).toArray(String[]::new)
        );
        String ADD_SUPPLIER_OPTION = "+ Add New Supplier";
        supCombo.addItem(ADD_SUPPLIER_OPTION);

        JPanel supplierPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        supplierPanel.add(supCombo);

        JButton infoBtn = createButton("Info");
        JButton deleteBtn = createButton("Delete");
        supplierPanel.add(infoBtn);
        supplierPanel.add(deleteBtn);

        infoBtn.addActionListener(e -> {
            String selected = (String) supCombo.getSelectedItem();
            if (selected != null && !selected.equals(ADD_SUPPLIER_OPTION)) {
                Supplier sup = suppliers.stream()
                        .filter(s -> s.getName().equals(selected))
                        .findFirst()
                        .orElse(null);
                if (sup != null) {
                    JOptionPane.showMessageDialog(this,
                            "Name: " + sup.getName() + "\nPhone: " + sup.getPhone(),
                            "Supplier Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            String selected = (String) supCombo.getSelectedItem();
            if (selected != null && !selected.equals(ADD_SUPPLIER_OPTION)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete supplier '" + selected + "'?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        Supplier sup = db.getSuppliers().stream()
                                .filter(s -> s.getName().equals(selected))
                                .findFirst()
                                .orElse(null);
                        if (sup != null) {
                            db.deleteSupplier(sup.getId());
                            supCombo.removeItem(selected);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this,
                                "Error deleting supplier: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        supCombo.addActionListener(e -> {
            if (((String) supCombo.getSelectedItem()).equals(ADD_SUPPLIER_OPTION)) {
                String newSupplier = addNewSupplier();
                if (newSupplier != null) {
                    supCombo.removeItem(ADD_SUPPLIER_OPTION);
                    supCombo.addItem(newSupplier);
                    supCombo.addItem(ADD_SUPPLIER_OPTION);
                    supCombo.setSelectedItem(newSupplier);
                }
            }
        });

        JTextField name = new JTextField();
        JTextField qty = new JTextField();
        JComboBox<String> cat = new JComboBox<>(new String[]{"Ingredient", "Beverage", "Frozen", "DryGoods"});

        Object[] input = {
                "Item Name:", name,
                "Quantity:", qty,
                "Category:", cat,
                "Supplier:", supplierPanel
        };

        int option = JOptionPane.showConfirmDialog(this, input,
                "Add New Stock", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int quantity = Integer.parseInt(qty.getText());
                Supplier selectedSupplier = suppliers.stream()
                        .filter(s -> s.getName().equals(supCombo.getSelectedItem()))
                        .findFirst()
                        .orElse(null);

                StockItem item = new StockItem(0, name.getText(),
                        quantity, (String) cat.getSelectedItem(), selectedSupplier);
                        
                boolean itemExists = db.getStockItems().stream()
                    .anyMatch(e -> e.getName().equals(name.getText()) && e.getSupplier().equals(selectedSupplier));

                if (itemExists) {
                    JOptionPane.showMessageDialog(rootPane, "There's already an item with this name and supplier");
                    return;
                }
                db.insertStock(item);
                loadStock();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addStock() {
        int row = stockTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select stock first");
            return;
        }

        String input = JOptionPane.showInputDialog("Add quantity:");
        if (input == null) return;

        try {
            int amount = Integer.parseInt(input);
            StockItem item = db.getStockItems().get(row);
            item.setQuantity(item.getQuantity() + amount);
            db.updateStockQuantity(item.getId(), item.getQuantity());
            db.addTransaction(item.getSupplier().getId(), item.getId(), amount);
            loadStock();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void reduceStock() {
        int row = stockTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select stock first");
            return;
        }

        String input = JOptionPane.showInputDialog("Reduce quantity:");
        if (input == null) return;

        try {
            int amount = Integer.parseInt(input);
            StockItem item = db.getStockItems().get(row);
            item.reduceStock(amount);
            db.updateStockQuantity(item.getId(), item.getQuantity());
            db.addTransaction(item.getSupplier().getId(), item.getId(), -amount);
            loadStock();
        } catch (OutOfStockEx e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteStock() {
        int row = stockTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select stock to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete selected stock?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                StockItem item = db.getStockItems().get(row);
                db.deleteStock(item.getId());
                loadStock();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String addNewSupplier() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();

        Object[] input = {
                "Supplier Name:", name,
                "Phone:", phone,
        };

        int option = JOptionPane.showConfirmDialog(this,
                input, "Add Supplier", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                Supplier newSupplier = new Supplier(0,
                        name.getText(), phone.getText());
                db.insertSupplier(newSupplier);
                JOptionPane.showMessageDialog(this,
                        "Supplier added successfully!");
                return newSupplier.getName();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error adding supplier: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private void showTransactions() {
        try {
            List<Transaction> transactions = db.getTransactions();

            String[] columns = {"Supplier", "Item", "Category", "Number"};
            DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
            JTable table = new JTable(tableModel);
            styleTable(table);
            JScrollPane scrollPane = new JScrollPane(table);

            for (Transaction t : transactions) {
                tableModel.addRow(new Object[]{
                        t.getSupplier().getName(),
                        t.getItem().getName(),
                        t.getItem().getCategory(),
                        t.getNumber()
                });
            }

            JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            filterPanel.setBackground(new Color(240, 242, 245));

            Set<String> supplierNames = new HashSet<>();
            for (Transaction t : transactions) supplierNames.add(t.getSupplier().getName());
            JComboBox<String> supplierBox = new JComboBox<>(supplierNames.toArray(new String[0]));
            supplierBox.insertItemAt("All", 0);
            supplierBox.setSelectedIndex(0);

            Set<String> categories = new HashSet<>();
            for (Transaction t : transactions) categories.add(t.getItem().getCategory());
            JComboBox<String> categoryBox = new JComboBox<>(categories.toArray(new String[0]));
            categoryBox.insertItemAt("All", 0);
            categoryBox.setSelectedIndex(0);

            Set<String> itemNames = new HashSet<>();
            for (Transaction t : transactions) itemNames.add(t.getItem().getName());
            JComboBox<String> itemBox = new JComboBox<>(itemNames.toArray(new String[0]));
            itemBox.insertItemAt("All", 0);
            itemBox.setSelectedIndex(0);

            JButton resetBtn = createButton("Reset");

            filterPanel.add(new JLabel("Supplier:"));
            filterPanel.add(supplierBox);
            filterPanel.add(new JLabel("Category:"));
            filterPanel.add(categoryBox);
            filterPanel.add(new JLabel("Item:"));
            filterPanel.add(itemBox);
            filterPanel.add(resetBtn);

            JFrame frame = new JFrame("Supplier Transactions");
            frame.setSize(700, 400);
            frame.setLocationRelativeTo(null);
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(filterPanel, BorderLayout.SOUTH);
            frame.setVisible(true);

            ActionListener filterAction = e -> {
                tableModel.setRowCount(0);
                String selectedSupplier = (String) supplierBox.getSelectedItem();
                String selectedCategory = (String) categoryBox.getSelectedItem();
                String selectedItem = (String) itemBox.getSelectedItem();

                for (Transaction t : transactions) {
                    boolean matchSupplier = "All".equals(selectedSupplier)
                            || t.getSupplier().getName().equals(selectedSupplier);
                    boolean matchCategory = "All".equals(selectedCategory)
                            || t.getItem().getCategory().equals(selectedCategory);
                    boolean matchItem = "All".equals(selectedItem)
                            || t.getItem().getName().equals(selectedItem);

                    if (matchSupplier && matchCategory && matchItem) {
                        tableModel.addRow(new Object[]{
                                t.getSupplier().getName(),
                                t.getItem().getName(),
                                t.getItem().getCategory(),
                                t.getNumber()
                        });
                    }
                }
            };

            supplierBox.addActionListener(filterAction);
            categoryBox.addActionListener(filterAction);
            itemBox.addActionListener(filterAction);

            resetBtn.addActionListener(e -> {
                supplierBox.setSelectedIndex(0);
                categoryBox.setSelectedIndex(0);
                itemBox.setSelectedIndex(0);
                filterAction.actionPerformed(null);
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading transactions: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
