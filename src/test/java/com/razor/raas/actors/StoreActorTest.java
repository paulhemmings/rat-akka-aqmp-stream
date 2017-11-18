package com.razor.raas.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import com.razor.raas.models.RequestMessage;
import com.razor.raas.models.ResultMessage;
import com.razor.raas.storage.CassandraService;
import com.razor.raas.storage.ContentDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Message;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by phemmings on 11/1/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class StoreActorTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void createReceive() throws Exception {

        // given
        CassandraService cassandraService = mock(CassandraService.class);
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class, cassandraService), "test-store-actor");

        // given (result message)
        RequestMessage requestMessage = new RequestMessage(mock(Message.class));

        // when
        storeActor.tell(requestMessage, ActorRef.noSender());

        // then (how do i test it?)

        // given (result message)
        ResultMessage resultMessage = new ResultMessage(mock(RequestMessage.class), "result");

        // when
        TestProbe testProbe = new TestProbe(system);
        testProbe.send(storeActor, resultMessage);

        // then (wait)
        testProbe.expectNoMsg();

        // then (test)
        verify(cassandraService, times(2)).insert(
                anyString(), anyString(), any(ContentDocument.class)
        );

    }

}