package com.razor.raas.services;

import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * Created by phemmings on 10/26/17.
 * Provide JMS services
 */

public class JmsService {

    private final static Logger LOGGER = Logger.getLogger(JmsService.class);


    /**
     * Given a factory, username and password
     * Return a new queue connection
     *
     * @param factory
     * @param userName
     * @param password
     * @return
     * @throws JMSException
     */

    public Connection buildConnection(final ConnectionFactory factory,
                                      final String userName,
                                      final String password) throws JMSException {
        return factory.createConnection(userName, password);
    }

    /**
     * Given a connection
     * Return a new queue session
     *
     * @param connection
     * @return
     * @throws JMSException
     */

    public Session buildSession(final Connection connection) throws JMSException {
        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Given a session and a queue name
     * Return a new queue receiver
     *
     * @param session
     * @param destination
     * @return
     * @throws JMSException
     */

    public MessageConsumer buildReceiver(final Session session,
                                         final Destination destination) throws JMSException {
        return session.createConsumer(destination);
    }

    /**
     * Given a queue session and a queue name
     * Return a new queue sender
     *
     * @param session
     * @param destination
     * @return
     * @throws JMSException
     */

    public MessageProducer buildSender(final Session session,
                                       final Destination destination) throws JMSException {
        return session.createProducer(destination);
    }

    /**
     * Given a connection, a reciever and client implementation
     * Connect to reciever and continue to receive until no message available
     *
     * @param connection
     * @param consumer
     * @param client
     */

    public void onReceive(final Connection connection,
                          final MessageConsumer consumer,
                          final IReceiver client) {

        LOGGER.debug("onReceive -> start receiving");
        try {
            LOGGER.debug("onReceive -> start connection");
            connection.start();
            LOGGER.debug("onReceive -> start listening");
            while (true) {
                Message message = consumer.receive();
                if (message == null || !client.receive(message)) {
                    break;
                }
            }
        } catch (JMSException ex) {
            LOGGER.error("Fail", ex);
        } finally {
            try {
                LOGGER.debug("onReceive -> close connection");
                connection.close();
            } catch (JMSException jex) {
                LOGGER.debug("onReceive -> failed to close connection");
            }
        }
        LOGGER.debug("onReceive -> finished receiving");
    }

    /**
     * Given a session, a sender and a text message
     * Add the message to the queue
     *
     * @param session
     * @param producer
     * @param text
     * @throws JMSException
     */

    public void sendTextMessage(final Session session,
                                final MessageProducer producer,
                                final String text) throws JMSException {
        TextMessage jmsMessage = session.createTextMessage();
        jmsMessage.setText(text);
        producer.send(jmsMessage);
    }

    /**
     * Interfaces
     */


    public interface IReceiver {
        public Boolean receive(Message message);
    }

}
