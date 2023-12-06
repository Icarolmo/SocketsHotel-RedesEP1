package com.redesdecomputadores.ep1.message;

import java.io.Serializable;

public class MessageServer implements Serializable {
    private String sender;
    private String recipient;
    private boolean close_connection;
    private String[] content;
    public MessageServer(String sender, String recipient, boolean close_connection, String[] content){
        this.sender = sender;
        this.recipient = recipient;
        this.close_connection = close_connection;
        this.content = content;
    }
    public String getSender(){
        return this.sender;
    }
    public String getRecipient(){
        return this.recipient;
    }
    public boolean getClose_connection() {
        return this.close_connection;
    }
    public String[] getContent(){
        return this.content;
    }
    public int getContentLength(){
        return this.content.length;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    public void setClose_connection(boolean close_connection) {
        this.close_connection = close_connection;
    }
    public void setContent(String[] content) {
        this.content = content;
    }
}
