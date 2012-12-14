package com.avricot.avrilog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avricot.avrilog.rabbitmq.ConnectionListener;
import com.avricot.avrilog.rabbitmq.HaConnectionFactory;
import com.avricot.avrilog.rabbitmq.RabbitMQException;
import com.avricot.avrilog.trace.Trace;
import com.avricot.avrilog.trace.TraceException;
import com.avricot.avrilog.trace.TraceListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

public class AvrilogClient {
    private static final Logger LOG = LoggerFactory.getLogger(HaConnectionFactory.class);
    public final static String TRACE_QUEUE_NAME = "avrilog-trace";
    private static boolean confirmTrace = true;
    private final static Map<String, Map<String, Object>> queurArgs = new HashMap<String, Map<String, Object>>();
    private static HaConnectionFactory factory;
    private static String applicationName;
    private static List<TraceListener> traceListeners = new ArrayList<TraceListener>();
    private static MessagePack msgpack = new MessagePack();

    public static boolean isConfirmTrace() {
        return confirmTrace;
    }

    /**
     * When true, amqp will wait to have ack when sending a trace. This add
     * around 50ms for broker with disk storage (the broker flush the data to
     * the disk).<br />
     * Default : true
     */
    public static void setConfirmTrace(final boolean confirmTrace) {
        AvrilogClient.confirmTrace = confirmTrace;
    }

    /**
     * Add the given args to the trace queue. Useful for ha, for example :
     * <code>
     * Map<String, Object> args = new HashMap<String, Object>();
     * args.put("ha-mode", "all");
     * AvrilogClient.addTraceQueueArgs(args)
     * </code>
     */
    public static void addTraceQueueArgs(final Map<String, Object> args) {
        addQueueArg(TRACE_QUEUE_NAME, args);
    }

    private static void addQueueArg(final String queueName, final Map<String, Object> args) {
        if (!queurArgs.containsKey(queueName)) {
            queurArgs.put(queueName, new HashMap<String, Object>());
        }
        queurArgs.get(queueName).putAll(args);
    }

    public static void addTraceListener(final TraceListener listener) {
        traceListeners.add(listener);
    }

    /**
     * Init rabbitmq factory with default values.
     */
    public static void init(final String applicationName) throws RabbitMQException {
        init(applicationName, null, null, null, null);
    }

    /**
     * Init rabbitmq factory with custom values. Should be called after all
     * configurations.
     */
    public static void init(final String applicationName, final String connectionUrl, final String username, final String password, final String virtualHost)
            throws RabbitMQException {
        AvrilogClient.applicationName = applicationName;
        factory = new HaConnectionFactory(connectionUrl);
        if (username != null) {
            factory.setUsername(username);
        }
        if (password != null) {
            factory.setPassword(password);
        }
        if (virtualHost != null) {
            factory.setVirtualHost(virtualHost);
        }
        factory.addListener(new ConnectionListener() {
            @Override
            public void onConnect(final Connection c) throws IOException {
                c.createChannel().queueDeclare(TRACE_QUEUE_NAME, true, false, false, queurArgs.get(TRACE_QUEUE_NAME));
            }
        });
        factory.initConnection();
    }

    /**
     * Compress and send the given trace to rabbitMQ broker.
     */
    public static void trace(final Trace trace, final boolean confirm) throws TraceException {
        try {
            Channel channel = factory.getLocalThreadChannel(confirm);
            trace.setApplicationName(applicationName);
            for (TraceListener listener : traceListeners) {
                listener.beforeSend(trace);
            }
            byte[] compressedTrace = compressTrace(trace);
            channel.basicPublish("", TRACE_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, compressedTrace);
            if (confirm) {
                channel.waitForConfirmsOrDie();
            }
            LOG.debug("trace {} sent - ", trace);
        } catch (IOException e) {
            throw new TraceException("can't create a new channel or publish message, The connection is probably down.", e);
        } catch (InterruptedException e) {
            throw new TraceException("Can't retrieve ack from rabbitmq broker, trace can't be send.", e);
        }
    }

    /**
     * Compress and send the given trace to rabbitMQ broker. Use
     * AvrilogClient.confirmTrace as confirm value (default true)
     */
    public static void trace(final Trace trace) throws TraceException {
        trace(trace, confirmTrace);
    }

    /**
     * Compress the trace using messagepack.
     */
    protected static byte[] compressTrace(final Trace trace) throws TraceException {
        try {
            return msgpack.write(trace);
        } catch (IOException e) {
            throw new TraceException("can't serialize the trace.", e);
        }
    }
}
