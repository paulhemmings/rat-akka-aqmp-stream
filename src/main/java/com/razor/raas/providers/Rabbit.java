package com.razor.raas.providers;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Created by phemmings on 10/27/17.
 */

public class Rabbit implements JmsProvider {

    public ConnectionFactory buildConnectionFactory(String serverUrl) {
        RMQConnectionFactory connectionFactory = new RMQConnectionFactory();
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        connectionFactory.setHost(serverUrl);
        return connectionFactory;
    }

    public Destination buildDestination(String queueName) {
        RMQDestination jmsDestination = new RMQDestination();
        jmsDestination.setDestinationName(queueName);
        jmsDestination.setAmqp(true);
        jmsDestination.setAmqpQueueName(queueName);
        jmsDestination.setAmqpExchangeName(""); // default exchange (name is empty string)
        jmsDestination.setAmqpRoutingKey(queueName); // the name of queue as the routing key
        return jmsDestination;
    }

}
