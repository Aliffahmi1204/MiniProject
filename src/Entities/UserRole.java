package Entities;

public interface UserRole {

    boolean canAddNewStock();
    boolean canAddStock();
    boolean canReduceStock();
    boolean canDeleteStock();
    boolean canViewTransactions();
}
