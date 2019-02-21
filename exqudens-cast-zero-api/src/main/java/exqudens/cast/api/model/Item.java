package exqudens.cast.api.model;

public class Item {

    private String description;
    private Order order;

    public Item() {
    }

    public Item(String description, Order order) {
        this.description = description;
        this.order = order;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "Item{" +
                "description='" + description + '\'' +
                '}';
    }

}
