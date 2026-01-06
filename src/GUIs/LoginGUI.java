package GUIs;

import java.awt.*;
import java.sql.Connection;

import javax.swing.*;
import java.util.List;
import Data.Database;
import Entities.Admin;
import Entities.Staff;

public class LoginGUI extends JFrame {

    Database db;

    public LoginGUI() {

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
        
        setTitle("Login");
        setSize(350, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Restaurant Stock System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        form.add(new JLabel("Username:"));
        form.add(username);
        form.add(new JLabel("Password:"));
        form.add(password);

        add(form, BorderLayout.CENTER);

        JButton loginBtn = new JButton("Login");
        loginBtn.setFocusPainted(false);

        JPanel bottom = new JPanel();
        bottom.add(loginBtn);
        add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            List<Admin> admins = db.getAdmins();
            List<Staff> staffMembers = db.getStaffMembers();

            String user = username.getText();
            String pass = new String(password.getPassword());

            boolean found = false;
            for (Admin admin : admins) {
                if (admin.getName().equals(user) && admin.getPassword().equals(pass)) {
                    new RestaurantGUI(admin, db);
                    dispose();
                    found = true;
                    break;
                }
            }

            for (Staff staff : staffMembers) {
                if (staff.getName().equals(user) && staff.getPassword().equals(pass)) {
                    new RestaurantGUI(staff, db);
                    dispose();
                    found = true;
                    break;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }
}