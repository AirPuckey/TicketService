package com.rph.ticketservice;

/**
 * A SeatHoldExpiredException is thrown when a seatHold expires
 * before the reserve request was made.
 */
public class SeatHoldExpiredException extends Exception {

    public SeatHoldExpiredException() {
        super();
    }

    public SeatHoldExpiredException(String message) {
        super(message);
    }

    public SeatHoldExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeatHoldExpiredException(Throwable cause) {
        super(cause);
    }
}
