package com.avricot.avrilog.rabbitmq;

import java.io.IOException;

import com.rabbitmq.client.Connection;

public interface ConnectionListener {
    public void onConnect(Connection c) throws IOException;
}
