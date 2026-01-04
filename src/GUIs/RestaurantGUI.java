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
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            System.exit(0);
        }
    }

private void initUI() {
    setLayout(new BorderLayout());

    model = new DefaultTableModel(
            new String[]{"ID", "Item", "Qty", "Category", "Supplier"}, 0
    );
    stockTable = new JTable(model);
    loadStock();

    add(new JScrollPane(stockTable), BorderLayout.CENTER);

    JPanel panel = new JPanel();

    JButton addnewBtn = new JButton("Add New Stock");
    JButton addBtn = new JButton("Add Stock");
    JButton reduceBtn = new JButton("Reduce Stock");
    JButton deleteBtn = new JButton("Delete Stock");
    JButton refreshBtn = new JButton("Refresh");
    JButton transactionBtn = new JButton("Transactions"); 

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

    private void loadStock() {
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

        JButton infoBtn = new JButton("Info");
        JButton deleteBtn = new JButton("Delete");
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

        // Delete button action
        deleteBtn.addActionListener(e -> {
            String selected = (String) supCombo.getSelectedItem();
            if (selected != null && !selected.equals(ADD_SUPPLIER_OPTION)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete supplier '" + selected + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
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
                        JOptionPane.showMessageDialog(this, "Error deleting supplier: " + ex.getMessage());
                    }
                }
            }
        });

        // Add new supplier option
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

        int option = JOptionPane.showConfirmDialog(this, input, "Add New Stock", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                int quantity = Integer.parseInt(qty.getText());
                Supplier selectedSupplier = suppliers.stream()
                        .filter(s -> s.getName().equals(supCombo.getSelectedItem()))
                        .findFirst()
                        .orElse(null);

                StockItem item = new StockItem(0, name.getText(), quantity, (String) cat.getSelectedItem(), selectedSupplier);
                db.insertStock(item);
            
                loadStock();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
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
            db.addTransaction(item.getSupplier().getId(), item.getId(),  -amount);
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

        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected stock?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                StockItem item = db.getStockItems().get(row);
                db.deleteStock(item.getId());
                loadStock();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
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

        int option = JOptionPane.showConfirmDialog(this, input, "Add Supplier", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Supplier newSupplier = new Supplier(0, name.getText(), phone.getText());
                db.insertSupplier(newSupplier);
                JOptionPane.showMessageDialog(this, "Supplier added successfully!");
                return newSupplier.getName();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error adding supplier: " + e.getMessage());
            }
        }
        return null;
    }

private void showTransactions() {
    try {
        List<Transaction> transactions = db.getTransactions(); // ✅ fix typo: geTransactions → getTransactions

        String[] columns = {"Supplier", "Item", "Category", "Number"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Fill table initially
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                t.getSupplier().getName(),
                t.getItem().getName(),
                t.getItem().getCategory(),
                t.getNumber()
            });
        }

        // Build filter panel (bottom right)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Supplier filter
        Set<String> supplierNames = new HashSet<>();
        for (Transaction t : transactions) supplierNames.add(t.getSupplier().getName());
        JComboBox<String> supplierBox = new JComboBox<>(supplierNames.toArray(new String[0]));
        supplierBox.insertItemAt("All", 0);
        supplierBox.setSelectedIndex(0);

        // Category filter
        Set<String> categories = new HashSet<>();
        for (Transaction t : transactions) categories.add(t.getItem().getCategory());
        JComboBox<String> categoryBox = new JComboBox<>(categories.toArray(new String[0]));
        categoryBox.insertItemAt("All", 0);
        categoryBox.setSelectedIndex(0);

        // Item filter
        Set<String> itemNames = new HashSet<>();
        for (Transaction t : transactions) itemNames.add(t.getItem().getName());
        JComboBox<String> itemBox = new JComboBox<>(itemNames.toArray(new String[0]));
        itemBox.insertItemAt("All", 0);
        itemBox.setSelectedIndex(0);

        // Reset button
        JButton resetBtn = new JButton("Reset");

        filterPanel.add(new JLabel("Supplier:"));
        filterPanel.add(supplierBox);
        filterPanel.add(new JLabel("Category:"));
        filterPanel.add(categoryBox);
        filterPanel.add(new JLabel("Item:"));
        filterPanel.add(itemBox);
        filterPanel.add(resetBtn);

        // Frame setup
        JFrame frame = new JFrame("Supplier Transactions");
        frame.setSize(700, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(filterPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Filtering logic
        ActionListener filterAction = e -> {
            tableModel.setRowCount(0); // clear table
            String selectedSupplier = (String) supplierBox.getSelectedItem();
            String selectedCategory = (String) categoryBox.getSelectedItem();
            String selectedItem = (String) itemBox.getSelectedItem();

            for (Transaction t : transactions) {
                boolean matchSupplier = "All".equals(selectedSupplier) || t.getSupplier().getName().equals(selectedSupplier);
                boolean matchCategory = "All".equals(selectedCategory) || t.getItem().getCategory().equals(selectedCategory);
                boolean matchItem = "All".equals(selectedItem) || t.getItem().getName().equals(selectedItem);

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
            filterAction.actionPerformed(null); // reload all
        });

    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error loading transactions: " + e.getMessage());
    }
}

}
