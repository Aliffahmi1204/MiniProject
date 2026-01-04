import Data.Database;
import GUIs.RestaurantGUI;
import java.sql.Connection;
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

}
