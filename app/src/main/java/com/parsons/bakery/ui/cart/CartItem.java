package com.parsons.bakery.ui.cart;

public class CartItem {
    protected int id;
    protected String name;
    protected String count;
    protected String url;
    protected String customizations;

    public void setUrl(String url) {
        this.url = url;
    }
    public void setCount(String count) {
        this.count = count;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCustomizations(String customizations) {
        this.customizations = customizations;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public String getCount() {
        return count;
    }
    public String getName() {
        return name;
    }
    public String getCustomizations() {
        return customizations;
    }
    public int getId() {
        return id;
    }
}
