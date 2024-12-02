package com.parsons.bakery.ui.home;

public class HomeItem {
    String name;
    String url;

    public HomeItem(String name, String url) {
        setName(name);
        setUrl(url);
    }

    public HomeItem() {

    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
