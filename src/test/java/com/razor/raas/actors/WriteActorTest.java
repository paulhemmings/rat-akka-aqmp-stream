package com.razor.raas.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import com.razor.raas.Application;
import com.razor.raas.models.RequestMessage;
import com.razor.raas.models.ResultMessage;
import com.razor.raas.resources.MessageResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Message;

import static org.mockito.Mockito.*;

/**
 * Created by phemmings on 11/1/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class WriteActorTest {

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
        MessageResource messageResource = mock(MessageResource.class);
        ActorRef writeActor = system.actorOf(Props.create(WriteActor.class, messageResource), "test-read-actor");
        ResultMessage resultMessage = new ResultMessage(mock(RequestMessage.class), "result");

        doReturn(true).when(messageResource).isInitialized();
        doReturn(mock(Message.class)).when(messageResource).buildTextMessage("result", "result");
        doNothing().when(messageResource).write(any(Message.class));
        doNothing().when(messageResource).close();

        // when
        TestProbe testProbe = new TestProbe(system);
        testProbe.send(writeActor, resultMessage);

        // then (wait)
        testProbe.expectNoMsg();

        // then (test)
        verify(messageResource, times(1)).initialize(Application.Queues.URL, Application.Queues.RESULT, Application.Queues.USERNAME, Application.Queues.PASSWORD);
        verify(messageResource, times(1)).write(any(Message.class));
        verify(messageResource, times(1)).close();
    }
}