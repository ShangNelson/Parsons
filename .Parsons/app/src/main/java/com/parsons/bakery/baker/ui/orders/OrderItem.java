package com.parsons.bakery.baker.ui.orders;

/* ORDER ITEM
 * item
 * type
 * count
 * customizations
 * time
 */

public class OrderItem {
    String item;
    String type;
    int count;
    String customizations;
    String time;
    public String getCustomizations() {
        return customizations;
    }
    public String getType() {
        return type;
    }
    public String getItem() {
        return item;
    }
    public int getCount() {
        return count;
    }
    public String getTime() {
        return time;
    }
    public void setCustomizations(String customizations) {
        this.customizations = customizations;
    }
    public void setCount(int count) {
        this.count = count;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setItem(String item) {
        this.item = item;
    }
    public void setTime(String time) {
        this.time = time;
    }
}
