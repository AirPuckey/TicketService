package com.rph.ticketservice;

/**
 * A NoSeatsAvailableException is thrown when a request for a seatHold is made
 * but there are not sufficient adjacent seats available.
 */
public class NoSeatsAvailableException extends Exception {

    public NoSeatsAvailableException() {
        super();
    }

    public NoSeatsAvailableException(String message) {
        super(message);
    }

    public NoSeatsAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSeatsAvailableException(Throwable cause) {
        super(cause);
    }
}
