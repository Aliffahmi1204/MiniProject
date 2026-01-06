package Entities;

import Exceptions.InvalidQuantityEx;
import Exceptions.OutOfStockEx;

public class StockItem extends Saveable {
    private int quantity;
    private String category;
    private Supplier supplier;

    public StockItem(int id, String name, int quantity, String category, Supplier s) {
        super(name, id);
        this.quantity = quantity;
        this.category = category;
        this.supplier = s;
    }

    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
    public Supplier getSupplier() { return supplier; }

    public void setSupplier(Supplier s) { this.supplier = s; }
    public void setQuantity(int q)  { 
        this.quantity = q;
     }

     public void addQuantity(int amount) throws InvalidQuantityEx {
        if (amount <= 0) {
            throw new InvalidQuantityEx("Your amount can not be less and equal to 0");
        }

        this.quantity += amount;
     }

    public void reduceStock(int amount) throws OutOfStockEx, InvalidQuantityEx {
        if (amount > quantity) {
            throw new OutOfStockEx("Not enough stock to reduce");
        } else if (amount <= 0) {
            throw new InvalidQuantityEx("Your amount can not be less and equal to 0");
        }
        quantity -= amount;
    }
}