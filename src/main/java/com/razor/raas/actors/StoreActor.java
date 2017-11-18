package com.razor.raas.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.razor.raas.models.RequestMessage;
import com.razor.raas.models.ResultMessage;
import com.razor.raas.storage.CassandraService;
import com.razor.raas.storage.ContentDocument;
import org.apache.log4j.Logger;

/**
 * Created by phemmings on 10/30/17.
 * Store messages to the database (Cassandra?)
 */

public class StoreActor extends AbstractLoggingActor {

    private final static Logger LOGGER = Logger.getLogger(StoreActor.class);
    private final CassandraService service;

    static Props props() {
        return Props.create(StoreActor.class);
    }
    static Props props(CassandraService cs) {
        return Props.create(StoreActor.class, cs);
    }

    /**
     * Cassandra details
     */

    protected static class Storage {
        public static final String HOST_URL = "127.0.0.1";
        public static final String KEY_SPACE = "tmspoc";
        public static final String REQUEST_TABLE = "request";
        public static final String RESULT_TABLE = "result";
    }

    /**
     * Constructor
     */

    public StoreActor() {
        this(new CassandraService(Storage.HOST_URL));
    }

    public StoreActor(final CassandraService cs) {
        this.service = cs;
    }

    /**
     * Match On:
     * Request Message -> pass to store request
     * Result Message -> pass to store result
     * @return
     */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMessage.class, this::storeRequest)
                .match(ResultMessage.class, this::storeResult)
                .build();
    }

    /**
     * Given -> a result message
     * Process -> store the message in Cassandra
     * @param resultMessage : result message
     */

    private void storeResult(ResultMessage resultMessage) {
        LOGGER.debug("storeResult -> start -> " + resultMessage.getResult());

        // build content document

        ContentDocument contentDocument = new ContentDocument();
        contentDocument.createRow()
            .add("result", resultMessage.getResult())
            .add("createtime", resultMessage.getCreateTime())
            .add("uniqueid", resultMessage.getUniqueId())
            .add("requestuniqueid", resultMessage.getOriginalRequest().getUniqueId());

        // connect to Cassandra
        this.service.insert(Storage.KEY_SPACE, Storage.RESULT_TABLE, contentDocument);

        LOGGER.debug("storeResult -> complete");
    }

    /**
     * Given -> a request message
     * Process -> store the message in Cassandra
     * @param requestMessage : Request message
     */

    private void storeRequest(RequestMessage requestMessage) {
        LOGGER.debug("storeRequest -> start -> " + requestMessage.getRequest());

        // build content document

        ContentDocument contentDocument = new ContentDocument();
        contentDocument.createRow()
                .add("request", requestMessage.getRequest())
                .add("createtime", requestMessage.getCreateTime())
                .add("uniqueid", requestMessage.getUniqueId());

        // connect to Cassandra
        this.service.insert(Storage.KEY_SPACE, Storage.REQUEST_TABLE, contentDocument);

        LOGGER.debug("storeRequest -> complete");
    }
}
