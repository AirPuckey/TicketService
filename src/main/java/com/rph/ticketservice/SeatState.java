package com.rph.ticketservice;

import static com.rph.ticketservice.SeatState.Availability.AVAILABLE;
import static com.rph.ticketservice.SeatState.Availability.HELD;

public class SeatState extends Seat {

    public enum Availability {
        AVAILABLE,
        HELD,
        RESERVED
    }

    private Availability availability = AVAILABLE;

    public SeatState(int rowNum, int seatNumInRow, int bestness) {
        super(rowNum, seatNumInRow, bestness);
    }

    public boolean isAvailable() {
        return availability == AVAILABLE;
    }

    public void setAvailable() {
        availability = AVAILABLE;
    }

    public void setHeld() {
        availability = HELD;
    }
}

