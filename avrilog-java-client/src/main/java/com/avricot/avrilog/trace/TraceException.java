package com.avricot.avrilog.trace;

public class TraceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TraceException(final String message) {
        super(message);
    }

    public TraceException(final String message, final Throwable e) {
        super(message, e);
    }
}
