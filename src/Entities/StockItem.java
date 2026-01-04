package Entities;

import Exceptions.OutOfStockEx;

public class StockItem {
    private int id;
    private String name;
    private int quantity;
    private String category;
    private Supplier supplier;

    public StockItem(int id, String name, int quantity, String category, Supplier s) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.category = category;
        this.supplier = s;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public String getCategory() { return category; }
    public Supplier getSupplier() { return supplier; }

    public void setSupplier(Supplier s) { this.supplier = s; }
    public void setQuantity(int q) { this.quantity = q; }

    public void reduceStock(int amount) throws OutOfStockEx {
        if (amount > quantity) {
            throw new OutOfStockEx("Not enough stock to reduce");
        }
        quantity -= amount;
    }
}