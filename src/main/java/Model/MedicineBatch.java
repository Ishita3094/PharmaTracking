package Model;

public class MedicineBatch {

    public MedicineBatch(String batchId, String name, double price, int quantity) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.batchId = batchId;
    }
    private String name;
    private int quantity;
    private double price;
    private String batchId;

    public String getBatchId() {
        return batchId;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
