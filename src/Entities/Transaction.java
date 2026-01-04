package Entities;

public class Transaction {
    private Supplier supplier;
    private StockItem item;
    private int number;

    public Transaction(Supplier supplier, StockItem item, int number) {
        this.supplier = supplier;
        this.item = item;
        this.number = number;
    }

    public Supplier getSupplier() { return supplier; }
    public StockItem getItem() { return item; }
    public int getNumber() { return number; }
}
