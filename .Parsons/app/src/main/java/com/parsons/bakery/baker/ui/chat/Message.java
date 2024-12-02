package com.parsons.bakery.baker.ui.chat;

public class Message {
    protected String message;
    protected String sender;

    public Message(String message, String sender) {
        setMessage(message);
        setSender(sender);
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
