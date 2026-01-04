import Data.Database;
import GUIs.RestaurantGUI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        Database db = new Database();
        Connection con;

        try {
            con = db.connect();   
            db.loadAll();         

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (con == null) {
            System.out.println("Database connection failed");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            new RestaurantGUI();
        });
    }

public List<String[]> getSupplierTransactions() throws Exception {
    List<String[]> transactions = new ArrayList<>();
    Database db = new Database();
    Connection conn = db.connect(); 

    String sql = "SELECT s.name, si.name, si.quantity " +
                 "FROM suppliers s " +
                 "JOIN stock_items si ON s.id = si.supplier_id";

    PreparedStatement ps = conn.prepareStatement(sql);
    ResultSet rs = ps.executeQuery();

    while (rs.next()) {
        transactions.add(new String[]{
            rs.getString(1),
            rs.getString(2), 
            String.valueOf(rs.getInt(3)) 
        });
    }

    return transactions;
}

}
