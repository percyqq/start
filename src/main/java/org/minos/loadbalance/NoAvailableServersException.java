package org.minos.loadbalance;

/**
 * NoAvailableServersException
 *
 * @date 2019/9/18
 */
public class NoAvailableServersException extends RuntimeException {

    public NoAvailableServersException(String message) {
        super(message);
    }

    public NoAvailableServersException() {
    }
}
