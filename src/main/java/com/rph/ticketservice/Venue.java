package com.rph.ticketservice;

import java.util.List;

/**
 * The theatre venue. An instance of this class is needed to create a TicketService.
 */
public interface Venue {

    /**
     * Number of rows in this Venue.
     *
     * @return number of rows
     */
    int getNumRows();

    /**
     * Number of seats per row in this Venue
     *
     * @return number of seats per row
     */
    int getNumSeatsPerRow();

    /**
     * All of the seats in this venue, ordered by decreasing seat bestness.
     * This list is unmodifiable, as are the Seat instances contained therein.
     *
     * @return the unmodifiable ordered list
     */
    List<Seat> bestSeats();
}
