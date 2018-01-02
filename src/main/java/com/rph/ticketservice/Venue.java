package com.rph.ticketservice;

import java.util.List;

public interface Venue {

    int getNumRows();

    int getNumSeatsPerRow();

    List<Seat> bestSeats();
}
