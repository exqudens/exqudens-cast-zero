package exqudens.cast.api.model;

import java.util.List;

public class Order {

    private String number;
    private User user;
    private List<Item> items;

    public Order() {
    }

    public Order(String number, User user, List<Item> items) {
        this.number = number;
        this.user = user;
        this.items = items;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Order{" +
                "number='" + number + '\'' +
                '}';
    }

}
