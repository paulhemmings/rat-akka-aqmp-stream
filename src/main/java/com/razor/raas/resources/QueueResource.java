package com.razor.raas.resources;

import com.google.gson.Gson;
import com.razor.raas.providers.QueueProvider;
import com.razor.raas.services.QueueService;
import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * Created by phemmings on 10/30/17.
 */
public class QueueResource implements MessageResource {

    private final static Logger LOGGER = Logger.getLogger(QueueResource.class);

    private QueueService queueService = new QueueService();
    private QueueProvider queueProvider;

    private QueueConnection connection = null;
    private QueueSession session = null;
    private String queueName = null;

    public QueueService getQueueService() {
        return queueService;
    }

    public QueueProvider getQueueProvider() {
        return queueProvider;
    }

    public QueueResource(QueueProvider provider) {
        this.queueProvider = provider;
    }

    @Override
    public Boolean isInitialized() {
        return this.session != null && this.connection != null;
    }

    @Override
    public void initialize(final String serverUrl, final String queueName, final String userName, final String pwd) {
        QueueConnectionFactory factory = this.getQueueProvider().buildQueueConnectionFactory(serverUrl);
        try {
            this.connection = this.getQueueService().buildConnection(factory, userName, pwd);
            this.session = this.getQueueService().buildSession(connection);
            this.queueName = queueName;
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
    public void reading(final MessageResource.Reader reader) throws JMSException {
        QueueReceiver receiver = this.getQueueService().buildReceiver(this.session, this.queueName);
        this.getQueueService().onReceive(this.connection, receiver, message -> reader.read(message));
    }

    @Override
    public void writing(final MessageResource.Writer writer) throws JMSException {
        QueueSender sender = this.getQueueService().buildSender(this.session, this.queueName);
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
        MessageProducer sender = this.getQueueService().buildSender(this.session, this.queueName);
        sender.send(message);
    }

}
