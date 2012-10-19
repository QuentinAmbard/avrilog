package com.avricot.avrilog;

import java.io.IOException;

import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avricot.avrilog.rabbitmq.ConnectionListener;
import com.avricot.avrilog.rabbitmq.HaConnectionFactory;
import com.avricot.avrilog.rabbitmq.RabbitMQException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class AvrilogClient {
    private static final Logger LOG = LoggerFactory.getLogger(HaConnectionFactory.class);
    private final static String QUEUE_NAME = "avrilog-trace";
    private static HaConnectionFactory factory;

    public static void init() throws RabbitMQException {
        factory = new HaConnectionFactory();
        factory.addListener(new ConnectionListener() {
            @Override
            public void onConnect(final Connection c) throws IOException {
                c.createChannel().queueDeclare(QUEUE_NAME, true, false, false, null);
            }
        });
        factory.setHost("localhost");
        factory.initConnection();
    }

    public static void trace(final Trace trace) throws TraceException {
        try {
            Channel channel = factory.getLocalThreadChannel(true);
            byte[] compressedTrace = compressTrace(trace);
            channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, compressedTrace);
            channel.waitForConfirmsOrDie();
            LOG.debug("trace {} sent - ", trace);
        } catch (IOException e) {
            throw new TraceException("can't create a new channel or publish message, The connection is probably down.", e);
        } catch (InterruptedException e) {
            throw new TraceException("Can't retrieve ack from rabbitmq broker, trace can't be send.", e);
        }
    }

    protected static byte[] compressTrace(final Trace trace) throws IOException {
        MessagePack msgpack = new MessagePack();
        return msgpack.write(trace);
    }
}
