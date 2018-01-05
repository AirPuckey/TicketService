package com.rph.ticketservice;

/**
 * A NoSeatsAvailableException is thrown when a request for a seatHold is made
 * but there are not sufficient adjacent seats available.
 */
public class SeatsUnavailableException extends Exception {

    public SeatsUnavailableException() {
        super();
    }

    public SeatsUnavailableException(String message) {
        super(message);
    }

    public SeatsUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeatsUnavailableException(Throwable cause) {
        super(cause);
    }
}
