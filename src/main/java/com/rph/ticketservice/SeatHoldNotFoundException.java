package com.rph.ticketservice;

/**
 * A SeatHoldNotFoundException is thrown when the SeatHold specified by a seat hold ID
 * cannot be found.
 */
public class SeatHoldNotFoundException extends Exception {

    public SeatHoldNotFoundException() {
        super();
    }

    public SeatHoldNotFoundException(String message) {
        super(message);
    }

    public SeatHoldNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeatHoldNotFoundException(Throwable cause) {
        super(cause);
    }
}
