package com.parsons.bakery.client;

import java.util.List;

public class orderItem {
    public List<String> customizations;

    public orderItem(List<String> customizations) {
        this.customizations = customizations;
    }

    public void setCustomizations(List<String> customizations) {
        this.customizations = customizations;
    }

    public List<String> getCustomizations() {
        return customizations;
    }
}
