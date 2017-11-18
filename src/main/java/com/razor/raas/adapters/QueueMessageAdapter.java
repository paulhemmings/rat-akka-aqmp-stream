package com.razor.raas.adapters;

public interface QueueMessageAdapter<T> {
    byte[] toQueueItem(T message);
    T toMessage(byte[] queueItem);
    T toMessage(javax.jms.Message queueItem);
}