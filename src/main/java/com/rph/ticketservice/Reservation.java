package com.rph.ticketservice;

public class Reservation {

    private final SeatHold seatHold;

    private final String reservationId;

    public Reservation(SeatHold seatHold, String reservationId) {
        this.seatHold = seatHold;
        this.reservationId = reservationId;
    }

    public SeatHold getSeatHold() {
        return seatHold;
    }

    public String getReservationId() {
        return reservationId;
    }
}
