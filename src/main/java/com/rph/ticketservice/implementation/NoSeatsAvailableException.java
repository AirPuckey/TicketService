package com.rph.ticketservice.implementation;

/**
 * A NoSeatsAvailableException is thrown when a request for a seatHold is made
 * but there are not sufficient adjacent seats available.
 */
class NoSeatsAvailableException extends Exception {

    NoSeatsAvailableException() {
        super();
    }

    NoSeatsAvailableException(String message) {
        super(message);
    }

    NoSeatsAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    NoSeatsAvailableException(Throwable cause) {
        super(cause);
    }
}
