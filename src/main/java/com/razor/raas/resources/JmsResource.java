package com.razor.raas.resources;

import com.google.gson.Gson;
import com.razor.raas.providers.JmsProvider;
import com.razor.raas.providers.Rabbit;
import com.razor.raas.services.JmsService;
import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * Created by phemmings on 10/30/17.
 */

public class JmsResource implements MessageResource {

    private final static Logger LOGGER = Logger.getLogger(JmsResource.class);
    private JmsService jmsService = new JmsService();
    private JmsProvider jmsProvider = new Rabbit();

    private Connection connection = null;
    private Session session = null;
    private Destination destination = null;

    public JmsService getJmsService() {
        return jmsService;
    }

    public JmsProvider getJmsProvider() {
        return jmsProvider;
    }

    public JmsResource(JmsProvider provider) {
        this.jmsProvider = provider;
    }

    @Override
    public Boolean isInitialized() {
        return this.session != null && this.connection != null;
    }

    @Override
    public void initialize(final String serverUrl, final String queueName, final String userName, final String pwd) {
        ConnectionFactory factory = this.getJmsProvider().buildConnectionFactory(serverUrl);
        try {
            this.connection = this.getJmsService().buildConnection(factory, userName, pwd);
            this.session = this.getJmsService().buildSession(connection);
            this.destination = this.getJmsProvider().buildDestination(queueName);
            this.session.createQueue(queueName);
        } catch (JMSException ex) {
            LOGGER.error("Unable to initialize", ex);
        }
    }

    @Override
    public void close() {
        try {
            this.session.close();
            this.connection.close();
        } catch (JMSException ex) {
            LOGGER.error("Unable to clean up", ex);
        }
    }

    @Override
    public void reading(final Reader reader) throws JMSException {
        MessageConsumer receiver = this.getJmsService().buildReceiver(this.session, this.destination);
        this.getJmsService().onReceive(this.connection, receiver, message -> reader.read(message));
    }

    @Override
    public void writing(final Writer writer) throws JMSException {
        MessageProducer sender = this.getJmsService().buildSender(this.session, this.destination);
        Message message;
        do {
            message = writer.write();
            if (message != null) {
                sender.send(message);
            }
        }
        while(message != null);
    }

    @Override
    public Message buildTextMessage(final String property, final String value) {
        try {
            Message message = this.session.createTextMessage();
            message.setStringProperty(property, new Gson().toJson(value));
            return message;
        } catch (JMSException e) {
            LOGGER.error("build -> failed to build the message", e);
        }
        return null;
    }

    @Override
    public void write(final Message message) throws JMSException {
        LOGGER.debug("write -> " + message);
        MessageProducer sender = this.getJmsService().buildSender(this.session, this.destination);
        sender.send(message);
    }

}
