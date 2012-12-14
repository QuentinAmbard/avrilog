/*
 * Copyright 2010 Josh Devins
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avricot.avrilog.rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 
 *
 */
public class HaConnectionFactory extends ConnectionFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HaConnectionFactory.class);
    private final Address[] addresses;

    public HaConnectionFactory(final String connectionUrl) {
        super();
        ArrayList<Address> adds = new ArrayList<Address>();
        if (connectionUrl != null) {
            String[] urls = connectionUrl.split(",");
            for (String url : urls) {
                String host = url.substring(0, url.indexOf(":"));
                String port = url.substring(url.indexOf(":") + 1);
                adds.add(new Address(host, Integer.valueOf(port)));
                LOG.debug("rabbitmq node address : {}:{}", host, port);
            }
            addresses = adds.toArray(new Address[adds.size()]);
        } else {
            addresses = null;
        }
    }

    /**
     */
    private class HaShutdownListener implements ShutdownListener {
        @Override
        public void shutdownCompleted(final ShutdownSignalException cause) {
            if (!cause.isInitiatedByApplication()) {
                Object reason;
                if (cause.isHardError()) {
                    Connection conn = (Connection) cause.getReference();
                    reason = cause.getReason();
                    LOG.warn("Connection error on {} : {}", conn.getAddress().toString(), cause.getMessage());
                } else {
                    Channel ch = (Channel) cause.getReference();
                    reason = ch.getCloseReason();
                    LOG.warn("Channel error: {}", cause.getMessage());
                }
                if (reason != null) {
                    LOG.warn("Shutdown signal caught: {}", reason);
                }

            }
            reconnect();
        }

    }

    private class ReconnectionTask extends Thread {
        @Override
        public void run() {
            String addressesAsString = "";
            if (LOG.isDebugEnabled()) {
                LOG.info("Reconnection starting, sleeping: addresses=" + addressesAsString + ", wait=" + reconnectionDelay);
            }

            // TODO: Add max reconnection attempts
            boolean connected = false;
            while (!connected) {
                try {
                    Thread.sleep(reconnectionDelay);
                } catch (InterruptedException ie) {
                    LOG.warn("Reconnection timer thread was interrupted, ignoring and reconnecting now");
                }
                connected = tryToConnect();
            }
        }
    }

    public static final Map<Boolean, ThreadLocal<Channel>> confirmedChannelThreadLocal = new HashMap<Boolean, ThreadLocal<Channel>>();
    static {
        confirmedChannelThreadLocal.put(true, new ThreadLocal<Channel>());
        confirmedChannelThreadLocal.put(false, new ThreadLocal<Channel>());
    }

    private final AtomicBoolean reconnectionInProgress = new AtomicBoolean(false);
    private long reconnectionDelay = 5000;
    private Connection connection;

    private final List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();

    public void addListener(final ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Try to reconnect, start a ReconnectionTask, no
     */
    private void reconnect() {
        boolean inprogress = reconnectionInProgress.getAndSet(true);
        // No reconnection in progress, init a new one.
        if (!inprogress) {
            new ReconnectionTask().start();
        }
    }

    /**
     * Init a new connection to the database.
     */
    public void initConnection() {
        boolean inprogress = reconnectionInProgress.getAndSet(true);
        // No reconnection in progress, init a new one.
        if (!inprogress) {
            if (!tryToConnect()) {
                new ReconnectionTask().start();
            }
        }
    }

    private boolean tryToConnect() {
        try {
            if (addresses == null || addresses.length == 0) {
                connection = super.newConnection();
            } else {
                connection = super.newConnection(addresses);
            }
            connection.addShutdownListener(new HaShutdownListener());
            LOG.info("Reconnection complete");
            try {
                for (ConnectionListener listener : listeners) {
                    listener.onConnect(connection);
                }
            } catch (IOException ioe) {
                LOG.error("Initialisation error, ", ioe);
                connection.close((int) (reconnectionDelay * 2 / 3));
                return false;
            }
            reconnectionInProgress.set(false);
            return true;
        } catch (IOException ioe) {
            LOG.error("Connection failed, broker is probably down, schedule a new reconnection", ioe);
            reconnect();
        } catch (Exception e) {
            LOG.error("Connection failed, something really bad happened, schedule a new reconnection ", e);
            reconnect();
        }
        return false;
    }

    /**
     * Create a new {@link Channel} from the current connection.
     * 
     * @param confirm
     *            true if the channel is selected as confirmed (so that we can
     *            wait brocker ack)
     * 
     */
    public Channel createChannel(final boolean confirm) throws RabbitMQException {
        if (reconnectionInProgress.get()) {
            throw new RabbitMQReconnectionException();
        }
        try {
            Channel channel = connection.createChannel();
            if (channel == null) {
                throw new RabbitMQException(
                        "Can't create the channel (get a null channel from the connection). Your application is probably requesting too many channel. Try using/not using localthread.");
            }
            if (confirm) {
                channel.confirmSelect();
            }
            return channel;
        } catch (IOException e) {
            throw new RabbitMQException("Can't create the channel.", e);
        }
    }

    /**
     * Get a channel, try to get one from the current thread, if not create a
     * new one.
     * 
     * @param confirm
     *            true if the channel is selected as confirmed (so that we can
     *            wait brocker ack)
     */
    public Channel getLocalThreadChannel(final boolean confirm) throws RabbitMQException {
        if (reconnectionInProgress.get()) {
            throw new RabbitMQReconnectionException();
        }
        Channel channel = confirmedChannelThreadLocal.get(confirm).get();
        if (channel == null || !channel.isOpen()) {
            Channel ch = createChannel(confirm);
            confirmedChannelThreadLocal.get(confirm).set(ch);
            return ch;
        } else {
            return channel;
        }

    }

    public long getReconnectionDelay() {
        return reconnectionDelay;
    }

    public void setReconnectionDelay(final long reconnectionDelay) {
        this.reconnectionDelay = reconnectionDelay;
    }

}
