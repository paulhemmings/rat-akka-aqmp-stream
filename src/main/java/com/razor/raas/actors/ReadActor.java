package com.razor.raas.actors;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.razor.raas.models.RequestMessage;
import org.apache.log4j.Logger;

import javax.jms.Message;

/**
 * Created by phemmings on 10/30/17.
 * Take the message from the JMS queue
 * Transform here or pass to transformer
 */

public class ReadActor extends AbstractLoggingActor {

    private final static Logger LOGGER = Logger.getLogger(ReadActor.class);
    private final ActorRef workerActor;
    private final ActorRef storeActor;

    static Props props(ActorRef workerActor, ActorRef storeActor) {
        return Props.create(ReadActor.class, workerActor, storeActor);
    }

    /**
     * Constructor
     * Create two actors to do the actual work.
     * Worker : This will process the request message
     * Store : This will store the request messages for complete audit trail
     */

    public ReadActor() {
        this.workerActor = getContext().actorOf(WorkerActor.props(), "worker-actor");
        this.storeActor = getContext().actorOf(StoreActor.props(), "store-request-actor");
    }

    public ReadActor(ActorRef workerActor, ActorRef storeActor) {
        this.workerActor = workerActor;
        this.storeActor = storeActor;
    }

    /**
     * Match on:
     * JMS Message -> pass on to work and store actors
     *
     * @return receiver
     */

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Message.class, request -> {
                    LOGGER.debug("reading -> received request -> wrap the request");
                    RequestMessage requestMessage = new RequestMessage(request);
                    LOGGER.debug("reading -> received request -> tell worker actor");
                    this.workerActor.tell(requestMessage, self());
                    LOGGER.debug("reading -> received request -> tell store actor");
                    this.storeActor.tell(requestMessage, self());
                    LOGGER.debug("reading -> received request -> finish");
                }).build();
    }
}
