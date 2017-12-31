package com.rph.ticketservice.implementation;

/**
 * A {@code Reservation} contains the information about a reservation.
 * A Reservation is immutable.
 */
public class Reservation {

    /** The seatHold from which this reservation was derived. */
    private final SeatHold seatHold;

    /** The reservation ID associated with this reservation. */
    private final String reservationId;

    /**
     * Constructs a new immutable Reservation.
     *
     * @param seatHold the setHold
     * @param reservationId the reservationId
     */
    Reservation(SeatHold seatHold, String reservationId) {
        this.seatHold = seatHold;
        this.reservationId = reservationId;
    }

    /**
     * The seatHold associated with this reservation.
     *
     * @return the seatHold
     */
    public SeatHold getSeatHold() {
        return seatHold;
    }

    /**
     * The reservation ID associated with this reservation.
     *
     * @return the reservationId
     */
    public String getReservationId() {
        return reservationId;
    }
}
