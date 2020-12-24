package org.minos.discover.client.http;

public class NotSupportMethodException extends RuntimeException {

    public NotSupportMethodException(String message) {
        super(message);
    }

    public NotSupportMethodException() {
    }
}
