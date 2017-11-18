package com.razor.raas.providers;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

/**
 * Created by phemmings on 10/27/17.
 */

public interface JmsProvider {

    ConnectionFactory buildConnectionFactory(String serverUrl);
    Destination buildDestination(String queueName);
}
