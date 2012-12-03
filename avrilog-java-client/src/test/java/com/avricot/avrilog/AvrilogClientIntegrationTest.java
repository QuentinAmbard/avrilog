package com.avricot.avrilog;

import org.junit.Test;

import com.avricot.avrilog.rabbitmq.RabbitMQReconnectionException;
import com.avricot.avrilog.trace.Trace;
import com.avricot.avrilog.trace.User;

public class AvrilogClientIntegrationTest {
    /**
     * Usage : broker is down during startup, the app should try to reconnect in
     * a separate thread, create connection when the broker is up and init the
     * queues.
     */
    @Test
    public void initBrokerDown() {
        try {
            AvrilogClient.init("test");
            AvrilogClient.trace(getTrace("t"));
        } catch (RabbitMQReconnectionException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AvrilogClient.trace(getTrace("test"));
    }

    /**
     * Start when broker is up, create queues.
     */
    @Test
    public void init() {
        AvrilogClient.init("test");
        AvrilogClient.trace(getTrace("test"));
    }

    /**
     * The broker crash, the app should reconnect when tu broker is up.
     */
    @Test
    public void reconnection() {
        AvrilogClient.init("test");
        for (int i = 0; i < 25; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                AvrilogClient.trace(getTrace("test" + i));
            } catch (RabbitMQReconnectionException e) {
                System.err.println("reconnection in progress :" + e.getMessage());
            }
        }
    }

    private Trace getTrace(final String category) {
        return new Trace().setUser(new User().setFirstname("firstname")).setCategory(category).addData("key", "value");
    }
}
