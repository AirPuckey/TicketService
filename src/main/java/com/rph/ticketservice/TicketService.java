package com.rph.ticketservice;

import com.rph.ticketservice.implementation.SeatHold;

public interface TicketService {
    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    int numSeatsAvailable();

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information, or null if sufficent adjacent seats are not available
     */
    SeatHold findAndHoldSeats(int numSeats, String customerEmail);

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code, or null if the specified
     * seatHold has expired
     */
    String reserveSeats(int seatHoldId, String customerEmail);
}

