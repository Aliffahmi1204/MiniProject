package Entities;

public abstract class Saveable {

    protected int id;
    protected String name;

    public Saveable(String name, int id) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

}
