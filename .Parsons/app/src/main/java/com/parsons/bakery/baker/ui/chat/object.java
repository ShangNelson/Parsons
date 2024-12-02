package com.parsons.bakery.baker.ui.chat;

public class object {
    private String displayText;
    private String hiddenValue;
    public object() {}
    public object(String displayText, String hiddenValue) {
        this.displayText = displayText;
        this.hiddenValue = hiddenValue;
    }
    public String getDisplayText() {
        return displayText;
    }
    public String getHiddenValue() {
        return hiddenValue;
    }
    @Override
    public String toString() {
        return displayText;
    }
}
