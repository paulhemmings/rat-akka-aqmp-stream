package com.razor.raas;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.razor.raas.actors.ReadActor;
import com.razor.raas.adapters.SimpleStringMessageAdapter;
import com.razor.raas.factories.MessageResourceFactory;
import com.razor.raas.providers.Rabbit;
import com.razor.raas.resources.MessageResource;
import org.apache.log4j.Logger;

import javax.jms.JMSException;
import java.util.Scanner;

/**
 * Created by phemmings on 10/26/17.
 * Create requests, listen for results, and monitor both queues
 */

public class Application {

    private final static Logger LOGGER = Logger.getLogger(Application.class);

    public static class Queues {
        private Queues() { /* do not instantiate */ }
        public static final String URL = "tcp://localhost:7222";
        public static final String USERNAME = "";
        public static final String PASSWORD = "";
        public static final String REQUEST = "request.queue";
        public static final String RESULT = "result.queue";
    }

    /**
     * Entry Point
     *
     * @param args
     * @throws JMSException
     */

    public static void main(String args[]) throws JMSException {
        LOGGER.debug("main -> start");
        if (args.length == 0) {
            new Application().process();
        } else if ("read".equalsIgnoreCase(args[0])) {
            new Application().read();
        } else if ("write".equalsIgnoreCase(args[0]) && args.length > 1) {
            new Application().send(args[1]);
        } else if ("write".equalsIgnoreCase(args[0])) {
            new Application().write();
        }
        LOGGER.debug("main -> complete");
    }

    /**
     * Monitor the result JMS queue for new messages
     * When a message appears, write it to debug
     * Drop out if the result message says "stop"
     * @throws JMSException
     */

    public void read() throws JMSException {
        LOGGER.debug("reading -> start");
        SimpleStringMessageAdapter messageAdapter = new SimpleStringMessageAdapter();
        MessageResource inputQueue = new MessageResourceFactory().build(new Rabbit());
        inputQueue.initialize(Queues.URL, Queues.RESULT, Queues.USERNAME, Queues.PASSWORD);
        inputQueue.reading(message -> {
            LOGGER.debug("reading result -> " + messageAdapter.toMessage(message));
            try {
                String result = message.getStringProperty("result");
                LOGGER.debug("reading result -> result -> " + result);
                return !result.contains("stop");
            } catch (JMSException e) {
                LOGGER.error("reading -> error retrieving result from message", e);
                return false;
            }
        });
        inputQueue.close();
        LOGGER.debug("reading -> complete");
    }

    /**
     * Monitor STD-IN
     * On each new line read in, add it to the request JMS queue
     * Drop out if an empty line is encountered.
     * @throws JMSException
     */

    public void write() throws JMSException {
        LOGGER.debug("write -> start -> ");
        MessageResource inputQueue = new MessageResourceFactory().build(new Rabbit());
        inputQueue.initialize(Queues.URL, Queues.REQUEST, Queues.USERNAME, Queues.PASSWORD);
        if (inputQueue.isInitialized()) {
            Scanner input = new Scanner(System.in);
            while (input.hasNextLine()){
                String line = input.nextLine();
                if (line.isEmpty()) break;
                inputQueue.write(inputQueue.buildTextMessage("request", line));
            }
        }
        LOGGER.debug("write -> complete");
        inputQueue.close();
    }

    /**
     * Given a single request message
     * Add that message to the request JMS queue
     * @param work
     * @throws JMSException
     */

    public void send(String work) throws JMSException {
        LOGGER.debug("send -> start -> " + work);
        MessageResource inputQueue = new MessageResourceFactory().build(new Rabbit());
        inputQueue.initialize(Queues.URL, Queues.REQUEST, Queues.USERNAME, Queues.PASSWORD);
        if (inputQueue.isInitialized()) {
            LOGGER.debug("write request -> " + work);
            inputQueue.write(inputQueue.buildTextMessage("request", work));
        }
        LOGGER.debug("send - complete");
    }

    /**
     * Start a new Akka system. Create a reader
     * Monitor the request JMS queue.
     * When a message is received, "tell" the reader about it
     * Drop out if the request says "stop"
     * @throws JMSException
     */

    public void process() throws JMSException {

        LOGGER.debug("process -> start");

        // 0. start the Akka queue system and create an actor to take the messages

        final ActorSystem actorSystem = ActorSystem.create("messages-processor-system");
        final ActorRef readActor = actorSystem.actorOf(Props.create(ReadActor.class), "reading-actor");

        // 1. open the queue

        MessageResource inputQueue = new MessageResourceFactory().build(new Rabbit());
        inputQueue.initialize(Queues.URL, Queues.REQUEST, Queues.USERNAME, Queues.PASSWORD);
        if (inputQueue.isInitialized()) {
            inputQueue.reading(message -> {

                LOGGER.debug("process -> request received -> tell reader");
                readActor.tell(message, ActorRef.noSender());

                try {
                    String request = message.getStringProperty("request");
                    LOGGER.debug("process - request received -> " + request);
                    return !request.contains("stop");
                } catch (JMSException e) {
                    LOGGER.debug("process -> error trying to read request", e);
                    return false;
                }

            });
        }

        // 2. clean up

        LOGGER.debug("process -> close the queue");
        inputQueue.close();

        LOGGER.debug("process -> close the actors");
        actorSystem.terminate();

        LOGGER.debug("process -> complete");
    }
}
