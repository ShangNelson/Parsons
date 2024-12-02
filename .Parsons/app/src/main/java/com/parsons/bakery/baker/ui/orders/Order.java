package com.parsons.bakery.baker.ui.orders;

public class Order {
    String order;

    String item;
    String type;
    String count;
    String customizations;

    public Order(String item, String type, String count, String customizations) {
        this.item = item;
        this.type = type;
        this.count = count;
        this.customizations = customizations;
    }


    public Order(String order) {
        this.order = order;
    }

    public Order() {

    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getItem() {
        return item;
    }

    public String getType() {
        return type;
    }

    public String getCustomizations() {
        return customizations;
    }

    public String getCount() {
        return count;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public void setCustomizations(String customizations) {
        this.customizations = customizations;
    }
}
