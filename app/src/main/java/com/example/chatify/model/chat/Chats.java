package com.example.chatify.model.chat;

public class Chats {
    public String dataTime;
    public String textMessage;
    public String url;
    public String type;
    public String sender;
    public String receiver;
    public String mno;

    public Chats() {
    }

    public Chats(String dataTime, String textMessage, String url, String type, String sender, String receiver, String mno) {
        this.dataTime = dataTime;
        this.textMessage = textMessage;
        this.url = url;
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;

    }

    public String getMno() {
        return mno;
    }

    public void setMno(String mno) {
        this.mno = mno;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getType() {
        return (type != null) ? type : "TEXT";  // ✅ Default to TEXT if null
    }


    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
