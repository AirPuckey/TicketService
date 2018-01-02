package com.rph.ticketservice;

import com.rph.ticketservice.implementation.TicketServiceImpl;
import com.rph.ticketservice.implementation.VenueImpl;

public class Factory {

    public Venue createVenue(int numRows, int numSeatsPerRow, int bestRowNum) {
        return new VenueImpl(numRows, numSeatsPerRow, bestRowNum);
    }

    public TicketService createTicketService(Venue venue) {
        return new TicketServiceImpl(venue);
    }

    public TicketService createTicketService(Venue venue, int expireMillies) {
        return new TicketServiceImpl(venue, expireMillies);
    }
}
