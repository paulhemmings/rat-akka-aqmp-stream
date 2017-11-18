package com.razor.raas.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.razor.raas.models.RequestMessage;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Message;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created by phemmings on 11/1/17.
 */

@RunWith(MockitoJUnitRunner.class)
public class ReadActorTest {

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
        Message originalMessage = mock(Message.class);
        TestKit workerProbe = new TestKit(system);
        TestKit storeProbe = new TestKit(system);
        ActorRef readActor = system.actorOf(Props.create(ReadActor.class, workerProbe.getRef(), storeProbe.getRef()), "test-read-actor");

        doReturn("originalrequest").when(originalMessage).getStringProperty("request");

        // when
        readActor.tell(originalMessage, ActorRef.noSender());

        // then
        RequestMessage requestMessage = workerProbe.expectMsgClass(RequestMessage.class);
        Assert.assertNotNull(requestMessage);
        Assert.assertEquals(requestMessage.getInnerMessage(), originalMessage);
        Assert.assertEquals(requestMessage, storeProbe.expectMsgClass(RequestMessage.class));
    }

}