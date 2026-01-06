package Entities;

public class Supplier extends Saveable {
    private int id;
    private String name;
    private String phone;

    public Supplier(int id, String name, String phone) {
        super(name, id);
        this.phone = phone;
    }

    public String getPhone() { return phone; }
  
}