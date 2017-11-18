package com.razor.raas.resources;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by phemmings on 10/30/17.
 * Common interface for JMS (used by Rabbit) and JMS Queue (used by Tibco)
 */

public interface MessageResource {

    Boolean isInitialized();

    void initialize(String serverUrl, String queueName, String userName, String pwd);

    void close();

    void reading(Reader reader) throws JMSException;

    void writing(Writer writer) throws JMSException;

    Message buildTextMessage(String property, String value);

    void write(Message message) throws JMSException;

    // essentially: Function<Message, Boolean>
    interface Reader {
        Boolean read(Message message);
    }

    interface Writer {
        Message write();
    }
}
