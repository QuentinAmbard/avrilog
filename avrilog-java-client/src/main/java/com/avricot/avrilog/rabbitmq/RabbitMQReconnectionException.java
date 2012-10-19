package com.avricot.avrilog.rabbitmq;

public class RabbitMQReconnectionException extends RabbitMQException {
    private static final long serialVersionUID = 1L;

    public RabbitMQReconnectionException() {
        super("error, a reconnection is in process, can't execute this operation");
    }
}
