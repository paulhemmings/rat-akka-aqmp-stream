package com.razor.raas.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.razor.raas.models.RequestMessage;
import com.razor.raas.models.ResultMessage;
import org.apache.log4j.Logger;

/**
 * Created by phemmings on 10/30/17.
 * Convert the message request to a result message.
 */

public class WorkerActor extends AbstractLoggingActor {

    private final static Logger LOGGER = Logger.getLogger(WorkerActor.class);
    private final ActorRef writerActor;
    private final ActorRef storeActor;

    static Props props() {
        return Props.create(WorkerActor.class);
    }

    static Props props(ActorRef writerActor, ActorRef storeActor) {
        return Props.create(WorkerActor.class, writerActor, storeActor);
    }

    /**
     * Constructor
     * Generate instances of write and store actors
     */

    public WorkerActor() {
        this.writerActor = getContext().actorOf(WriteActor.props(), "writer-worker");
        this.storeActor = getContext().actorOf(StoreActor.props(), "store-result-actor");
    }

    public WorkerActor(ActorRef writerActor, ActorRef storeActor) {
        this.writerActor = writerActor;
        this.storeActor = storeActor;
    }

    /**
     * Match On:
     * Request Message -> pass on to write and store actors
     * @return
     */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMessage.class, request -> {
                    LOGGER.debug("worker -> received -> message request -> start");
                    // create a result message
                    LOGGER.debug("worker -> received -> message request -> create result");
                    ResultMessage resultMessage = this.doWork(request);
                    // give it to a new write actor
                    LOGGER.debug("worker -> received -> message request -> tell writer");
                    this.writerActor.tell(resultMessage, self());
                    // give it to a new store actor
                    LOGGER.debug("worker -> received -> message request -> tell store");
                    this.storeActor.tell(resultMessage, self());
                    LOGGER.debug("worker -> received -> message request -> complete");
                }).build();
    }

    /**
     * Given -> a request message
     * Return -> a result message
     * @param request : message
     * @return result : message
     */

    ResultMessage doWork(RequestMessage request) {
        LOGGER.debug("doWork -> start");
        ResultMessage resultMessage = new ResultMessage(request, "processed:" + request.getRequest());
        LOGGER.debug("doWork -> complete -> " + resultMessage.getResult());
        return resultMessage;
    }

}
