package com.rph.ticketservice;

/**
 * This class represents a seat hold (perhaps consumated in a Reservation, perhaps expired).
 */
public interface SeatHold {

    /**
     * The seat old ID.
     *
     * @return the unique (for this performance) ID of this SeatHold
     */
    int getSeatHoldId();

    /**
     * The number of seats in this SeatHold.
     *
     * @return the number of seats in this SeatHold.
     */
    int numSeatsHeld();

    /**
     * One of the seats in this SeatHold.
     *
     * @param index the index of this SeatHold (in the range of 0 to numSeatsHeld minus 1)
     * @return the specified seat
     */
    Seat getSeat(int index);

    /**
     * True if this SeatHold is held (not reserved or expired).
     *
     * @return true if the SeatHold is held, otherwise false
     */
    boolean isHeld();

    /**
     * True if this SeatHold is reserved (no longer held, and not expired).
     * This is a terminal state. Depending on circumstances, a SeatHold can
     * transition from held to reserved.
     *
     * @return true if the SeatHold is reserved, otherwise false
     */
    boolean isReserved();

    /**
     * True if this SeatHold is expired (was held, never got reserved).
     * This is a terminal state. Depending on circumstances, a SeatHold
     * can transition from held to expired.
     *
     * @return true if the SeatHold is expired, otherwise false
     */
    boolean isExpired();
}
