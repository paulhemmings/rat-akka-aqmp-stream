package com.razor.raas.providers;

import javax.jms.QueueConnectionFactory;

/**
 * Created by phemmings on 10/26/17.
 */

public interface QueueProvider {
    QueueConnectionFactory buildQueueConnectionFactory(final String serverUrl);
}
