package com.rph.ticketservice;

import java.util.List;

public class SeatHold {

    private enum State {
        HELD,
        RESERVED,
        EXPIRED
    }

    private final int seatHoldId;

    private final String customerEmail;

    private final List<Seat> seats;

    private State state = State.HELD;

    public SeatHold(int seatHoldId, String customerEmail, List<Seat> seats) {
        if (!isValidEmailAddress(customerEmail)) {
            throw new IllegalArgumentException("invalid email address: " + customerEmail);
        }
        this.seatHoldId = seatHoldId;
        this.customerEmail = customerEmail;
        this.seats = seats;
    }

    public int getSeatHoldId() {
        return seatHoldId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<Seat> getHeldSeats() {
        return seats;
    }

    public boolean isHeld() {
        return state == State.HELD;
    }

    public boolean isReserved() {
        return state == State.RESERVED;
    }

    public boolean isExpired() {
        return state == State.EXPIRED;
    }

    public boolean expire() {
        if (state == State.HELD) {
            state = State.EXPIRED;
        }
        return state == State.EXPIRED;
    }

    public void reserveSeats() throws HoldExpiredException {
        if (state == State.EXPIRED) {
            throw new HoldExpiredException();
        }
        state = State.RESERVED;
    }

    static boolean isValidEmailAddress(String customerEmail) {
        if (customerEmail == null) {
            return false;
        }
        // this is overly simplistic
        return customerEmail.matches("^[.a-zA-Z0-9]+@[a-zA-Z0-9]+(.[a-zA-Z]{2,})$");
    }
}
