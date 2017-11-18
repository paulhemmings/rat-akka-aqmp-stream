package com.razor.raas.factories;

import com.razor.raas.providers.JmsProvider;
import com.razor.raas.providers.QueueProvider;
import com.razor.raas.providers.Rabbit;
import com.razor.raas.resources.JmsResource;
import com.razor.raas.resources.MessageResource;
import com.razor.raas.resources.QueueResource;

/**
 * Created by phemmings on 10/30/17.
 * Builds a new instance of a MessageResource, based on the type of provider provided.
 */

public class MessageResourceFactory {

    public MessageResource build(JmsProvider jmsProvider) {
        return new JmsResource(jmsProvider);
    }

    public MessageResource build(QueueProvider queueProvider) {
        return new QueueResource(queueProvider);
    }

    public MessageResource build(String provider) throws MessageResourceFactoryException {
        if ("rabbit".equalsIgnoreCase(provider)) {
            return new JmsResource(new Rabbit());
        } else {
            throw new MessageResourceFactoryException("Invalid provider");
        }
    }

    public static class MessageResourceFactoryException extends Exception {
        public MessageResourceFactoryException(String message) {
            super(message);
        }
    }
}
