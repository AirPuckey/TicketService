package com.rph.ticketservice;

import com.rph.ticketservice.implementation.TicketServiceImpl;
import com.rph.ticketservice.implementation.VenueImpl;

/**
 * This class encapsulates everything needed to link to the implementation.
 */
public class Factory {

    /**
     * Creates a Venue.
     *
     * @param numRows number of rows
     * @param numSeatsPerRow number of seats per row
     * @param bestRowNum best row number in the venue
     * @return a new Venue
     */
    public Venue createVenue(int numRows, int numSeatsPerRow, int bestRowNum) {
        return new VenueImpl(numRows, numSeatsPerRow, bestRowNum);
    }

    /**
     * Creates a TicketService.
     *
     * @param venue the venue
     * @return a new TicketService
     */
    public TicketService createTicketService(Venue venue) {
        return new TicketServiceImpl(venue);
    }

    /**
     * Creates a TicketService.
     *
     * @param venue the venue
     * @param expireMillies number of milliseconds until an unreserved SeatHold is expired.
     * @return a new TicketService
     */
    public TicketService createTicketService(Venue venue, int expireMillies) {
        return new TicketServiceImpl(venue, expireMillies);
    }
}
