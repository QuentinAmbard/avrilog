package com.avricot.avrilog.rabbitmq;

public class RabbitMQException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RabbitMQException(final String message) {
        super(message);
    }

    public RabbitMQException(final String message, final Throwable e) {
        super(message, e);
    }
}
