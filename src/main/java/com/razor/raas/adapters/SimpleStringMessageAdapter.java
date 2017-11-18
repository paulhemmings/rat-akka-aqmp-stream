package com.razor.raas.adapters;

import com.google.gson.Gson;

public class SimpleStringMessageAdapter implements QueueMessageAdapter<String> {
    @Override
    public byte[] toQueueItem(String message) {
        return message.getBytes();
    }
    @Override
    public String toMessage(byte[] queueItem) {
        return new String((queueItem));
    }

    public String toMessage(javax.jms.Message queueItem) {
        return new Gson().toJson(queueItem);
    }
}

