package com.razor.raas.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.razor.raas.models.RequestMessage;
import com.razor.raas.models.ResultMessage;
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
public class WorkerActorTest {

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
    public void doWork() throws Exception {

        // given
        Message innerMessage = mock(Message.class);
        TestKit writeProbe = new TestKit(system);
        TestKit storeProbe = new TestKit(system);
        ActorRef workerActor = system.actorOf(Props.create(WorkerActor.class, writeProbe.getRef(), storeProbe.getRef()), "test-work-actor");
        RequestMessage requestMessage = new RequestMessage(innerMessage);

        doReturn("originalrequest").when(innerMessage).getStringProperty("request");

        // when
        workerActor.tell(requestMessage, ActorRef.noSender());

        // then
        ResultMessage resultMessage = writeProbe.expectMsgClass(ResultMessage.class);
        Assert.assertNotNull(resultMessage);
        Assert.assertEquals(resultMessage.getOriginalRequest(), requestMessage);
        Assert.assertTrue(resultMessage.getResult().contains(requestMessage.getRequest()));
        Assert.assertEquals(resultMessage, storeProbe.expectMsgClass(ResultMessage.class));
    }

}