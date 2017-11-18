package com.razor.raas.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.razor.raas.Application;
import com.razor.raas.factories.MessageResourceFactory;
import com.razor.raas.models.ResultMessage;
import com.razor.raas.providers.Rabbit;
import com.razor.raas.resources.MessageResource;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Created by phemmings on 10/30/17.
 * Write messages back to the JMS queue
 */

public class WriteActor extends AbstractLoggingActor {

    private final static Logger LOGGER = Logger.getLogger(WriteActor.class);
    private final MessageResource messageResource;

    static Props props() {
        return Props.create(WriteActor.class);
    }
    static Props props(MessageResource mr) {
        return Props.create(WriteActor.class, mr);
    }

    public WriteActor() {
        this.messageResource = new MessageResourceFactory().build(new Rabbit());
    }

    public WriteActor(MessageResource messageResource) {
        this.messageResource = messageResource;
    }

    /**
     * Match On:
     * Result message -> pass on to write result function
     * @return
     */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                    .match(ResultMessage.class, resultMessage -> {
                        LOGGER.debug("write -> received result -> write out");
                        this.writeResult(resultMessage);
                        LOGGER.debug("write -> complete");
                    }).build();
    }

    /**
     * Given -> a result message
     * Process -> write the message to the "result" JMS queue
     * @param resultMessage
     */

    private void writeResult(ResultMessage resultMessage) {
        LOGGER.debug("writeResult -> start -> " + resultMessage.getResult());
        try {
            this.messageResource.initialize(Application.Queues.URL, Application.Queues.RESULT, Application.Queues.USERNAME, Application.Queues.PASSWORD);
            if (this.messageResource.isInitialized()) {
                Message result = this.messageResource.buildTextMessage("result", resultMessage.getResult());
                if (result != null) {
                    this.messageResource.write(result);
                }
            }
        } catch (JMSException ex) {
            LOGGER.error("unable to write result to queue", ex);
        } finally {
            this.messageResource.close();
        }

        LOGGER.debug("writeResult -> finished");
    }
}
