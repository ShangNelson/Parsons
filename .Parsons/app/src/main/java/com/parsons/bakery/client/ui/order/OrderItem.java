package com.parsons.bakery.ui.order;

public class OrderItem {
    String name;
    String url;
    String description;
    String inner_category;
    String label;
    String req;
    int has;

    public OrderItem(String name, String url, String description, String inner_category, String req) {
        setName(name);
        setUrl(url);
        setDescription(description);
        setInner_category(inner_category);
        setReq(req);
    }

    public OrderItem(String name, String url, int has) {
        setName(name);
        setUrl(url);
        setHas(has);
    }

    public OrderItem(String label) {
        setLabel(label);
        setInner_category("label");
    }

    public String getName() {
        return name;
    }
    public String getUrl() {
        return url;
    }
    public String getDescription() {
        return description;
    }
    public String getInner_category() {
        return inner_category;
    }
    public String getLabel() {
        return label;
    }
    public int getHas() {
        return has;
    }
    public String getReq() {
        return req;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setInner_category(String inner_category) {
        this.inner_category = inner_category;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public void setHas(int has) {
        this.has = has;
    }
    public void setReq(String req) {
        this.req = req;
    }
}
