package Entities;

public class Admin extends Saveable implements UserRole {
    
    private String password;

    public Admin(int id, String name, String password) {
        super(name, id);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean canAddNewStock() {
        return true;
    }

    @Override
    public boolean canAddStock() {
        return true;
    }

    @Override
    public boolean canReduceStock() {
        return true;
    }

    @Override
    public boolean canDeleteStock() {
        return true;
    }

    @Override
    public boolean canViewTransactions() {
        return true;
    }

}
