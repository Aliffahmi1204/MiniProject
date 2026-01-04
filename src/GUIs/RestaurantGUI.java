package GUIs;

import Data.Database;
import Entities.StockItem;
import Entities.Supplier;
import Entities.Transaction;
import Exceptions.OutOfStockEx;
import java.awt.*;
import java.sql.Connection;
import java.util.List;
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
            db.addTransaction(item.getSupplier().getId(), item.getId(), -amount);
            loadStock();
        } catch (OutOfStockEx e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
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
        List<Transaction> transactions = db.geTransactions();

        String[] columns = {"Supplier", "Item", "Number"};
        Object[][] data = new Object[transactions.size()][3];

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            data[i][0] = t.getSupplier().getName();
            data[i][1] = t.getItem().getName();
            data[i][2] = t.getNumber();
        }


        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame("Supplier Transactions");
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(this);
        frame.add(scrollPane);
        frame.setVisible(true);

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
    }
}

}
