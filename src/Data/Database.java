package Data;

import Entities.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private Connection conn;
    private final List<StockItem> stockItems = new ArrayList<>();
    private final List<Supplier> suppliers = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<Staff> staffMembers = new ArrayList<>();
    private final List<Admin> admins = new ArrayList<>();

    private static final String URL = "jdbc:mysql://localhost:3306/miniproject?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; 

    public Connection connect() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(URL, USER, PASS);
        return conn;
    }

    

    private void seedDefaultSupplierIfEmpty() throws SQLException {
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) AS c FROM suppliers")) {
            rs.next();
            if (rs.getInt("c") == 0) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO suppliers(name, phone) VALUES (?, ?, ?)")) {
                    ps.setString(1, "Default Supplier");
                    ps.setString(2, "0123456789");
                    ps.setString(3, "123 Main Street");
                    ps.executeUpdate();
                }
            }
        }
    }

    public void loadAll() throws Exception {
        loadUsers();
        loadSuppliers();
        loadStock();
        loadTransactions();
    }

 
    private void loadSuppliers() throws SQLException {
        suppliers.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, phone FROM suppliers")) {
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone")
                ));
            }
        }
    }

    private void loadStock() throws SQLException {
        stockItems.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, quantity, category FROM stock")) {
            while (rs.next()) {
                stockItems.add(new StockItem(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getString("category"),
                            null
                ));
            }
        }
    }

    private void loadTransactions() throws SQLException {
        transactions.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transaction")) {
            while (rs.next()) {
                int supplierId = rs.getInt(2);
                int stockid = rs.getInt(3);
                Supplier supplier = suppliers.stream()
                        .filter(s -> s.getId() == supplierId)
                        .findFirst()
                        .orElse(null);
                StockItem stock = stockItems.stream()
                        .filter(s -> s.getId() == stockid)
                        .findFirst()
                        .orElse(null);
                if (supplier != null && stock != null) {
                    stock.setSupplier(supplier);
                    transactions.add(new Transaction(supplier, stock, rs.getInt(4)));
                }
            }
        }
    }

    private void loadUsers() throws SQLException {
        admins.clear();
        staffMembers.clear();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `user` WHERE role = 'Admin'")) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                admins.add(new Admin(id, name, password));
            }
        }
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `user` WHERE role = 'Staff'")) {
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                String password = rs.getString(3);
                staffMembers.add(new Staff(id, name, password));
            }
        }
    }

    public void insertStock(StockItem item) throws SQLException {
        if (item.getSupplier() == null) {
            throw new SQLException("Supplier cannot be null");
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO stock (name, quantity, category) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )) {
            ps.setString(1, item.getName());
            ps.setInt(2, item.getQuantity());
            ps.setString(3, item.getCategory());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = 0;
                if (keys.next()) id = keys.getInt(1);
                stockItems.add(new StockItem(
                        id == 0 ? item.getId() : id,
                        item.getName(),
                        item.getQuantity(),
                        item.getCategory(),
                        item.getSupplier()
                ));

                addTransaction(item.getSupplier().getId(), id, item.getQuantity());
            }
        }
    }

    public void updateStockQuantity(int id, int newQty) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE stock SET quantity = ? WHERE id = ?")) {
            ps.setInt(1, newQty);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
        stockItems.stream()
                .filter(s -> s.getId() == id)
                .findFirst()
                .ifPresent(s -> s.setQuantity(newQty));
    }

    public void deleteStock(int id) throws SQLException {
        deleteTransactions(id);

        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM stock WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        stockItems.removeIf(s -> s.getId() == id);
    }

    public void deleteSupplier(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM suppliers WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        suppliers.removeIf(s -> s.getId() == id);
    }

    public void deleteTransactions(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM transaction WHERE stockId = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
        transactions.removeIf(s -> s.getItem().getId() == id);
    }


    public void insertSupplier(Supplier s) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO suppliers (name, phone) VALUES (?, ?)")) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.executeUpdate();
            loadSuppliers(); 
        }
    }

    public void addTransaction(int supplierId, int itemId, int quantity) throws SQLException {
        String sql = "INSERT INTO transaction (supplierid, stockid, no) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setInt(2, itemId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
            loadTransactions();
        }
    }


    public List<StockItem> getStockItems() {
        return stockItems;
    }

    public List<Supplier> getSuppliers() {
        return suppliers;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Staff> getStaffMembers() {
        return staffMembers;
    }

    public List<Admin> getAdmins() {
        return admins;
    }

}