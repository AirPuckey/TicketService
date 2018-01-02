package com.rph.ticketservice;

public interface SeatHold {

    int getSeatHoldId();

    int numSeatsHeld();

    Seat getSeat(int index);

    boolean isHeld();

    boolean isReserved();

    boolean isExpired();
}
