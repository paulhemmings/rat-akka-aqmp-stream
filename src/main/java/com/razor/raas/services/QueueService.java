package com.razor.raas.services;

import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * Created by phemmings on 10/26/17.
 * Provide JMS services
 */

public class QueueService {

    private final static Logger LOGGER = Logger.getLogger(QueueService.class);


    /**
     * Given a factory, username and password
     * Return a new queue connection
     * @param factory
     * @param userName
     * @param password
     * @return
     * @throws JMSException
     */

    public QueueConnection buildConnection(final QueueConnectionFactory factory,
                                           final String userName,
                                           final String password) throws JMSException {
        return factory.createQueueConnection(userName, password);
    }

    /**
     * Given a connection
     * Return a new queue session
     * @param connection
     * @return
     * @throws JMSException
     */

    public QueueSession buildSession(final QueueConnection connection) throws JMSException {
        return connection.createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Given a session and a queue name
     * Return a new queue receiver
     * @param session
     * @param queueName
     * @return
     * @throws JMSException
     */

    public QueueReceiver buildReceiver(final QueueSession session,
                                       final String queueName) throws JMSException {
        Queue receiverQueue = session.createQueue(queueName);
        return session.createReceiver(receiverQueue);
    }

    /**
     * Given a queue session and a queue name
     * Return a new queue sender
     * @param session
     * @param queueName
     * @return
     * @throws JMSException
     */

    public QueueSender buildSender(final QueueSession session,
                                   final String queueName) throws JMSException {
        Queue senderQueue = session.createQueue(queueName);
        return session.createSender(senderQueue);
    }

    /**
     * Given a connection, a reciever and client implementation
     * Connect to reciever and continue to receive until no message available
     * @param connection
     * @param receiver
     * @param client
     */

    public void onReceive(final QueueConnection connection,
                          final QueueReceiver receiver,
                          final IReceiver client) {
        try {
            connection.start();
            while (true) {
                Message message = receiver.receive();
                if (message == null) {
                    break;
                }
                client.receive(message);
            }
            connection.close();
        } catch (JMSException ex) {
            LOGGER.error("Fail", ex);
        }
    }

    /**
     * Given a session, a sender and a text message
     * Add the message to the queue
     * @param session
     * @param sender
     * @param text
     * @throws JMSException
     */

    public void sendTextMessage(final QueueSession session,
                                final QueueSender sender,
                                final String text) throws JMSException {
        TextMessage jmsMessage = session.createTextMessage();
        jmsMessage.setText(text);
        sender.send(jmsMessage);
    }

    /**
     * Interfaces
     */


    public interface IReceiver {
        public void receive(Message message);
    }

}
