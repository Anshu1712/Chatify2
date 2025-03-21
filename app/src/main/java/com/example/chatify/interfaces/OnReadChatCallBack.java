package com.example.chatify.interfaces;

import com.example.chatify.model.chat.Chats;

import java.util.List;

public interface OnReadChatCallBack {

    void onReadSuccess(List<Chats> list);

    void onReadFailed();
}
