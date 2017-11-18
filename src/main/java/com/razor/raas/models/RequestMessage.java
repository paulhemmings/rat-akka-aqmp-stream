package com.razor.raas.models;

import com.razor.raas.actors.ReadActor;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Date;
import java.util.UUID;

/**
 * Created by phemmings on 10/30/17.
 * Container for JMS message
 */

public class RequestMessage {

    private final static Logger LOGGER = Logger.getLogger(ReadActor.class);
    private long createTime;
    private Message innerMessage;
    private String uniqueId;

    public RequestMessage(Message message) {
        this.uniqueId = UUID.randomUUID().toString();
        this.createTime = new Date().getTime();
        this.innerMessage = message;
    }

    public Message getInnerMessage() {
        return innerMessage;
    }
    public long getCreateTime() {
        return createTime;
    }
    public String getUniqueId() {
        return uniqueId;
    }

    public String getRequest() {
        try {
            return this.getInnerMessage().getStringProperty("request");
        } catch (JMSException e) {
            LOGGER.error("unable to extract request. store request received", e);
            return e.getMessage();
        }
    }
}
