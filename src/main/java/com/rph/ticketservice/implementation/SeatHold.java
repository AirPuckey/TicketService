package com.rph.ticketservice.implementation;

import java.util.List;


/**
 * A {@code SeatHold} contains the information necessary to hold or reserve a set of seats.
 */
public class SeatHold {

    /** Possible states of a seatHold, */
    private enum State {
        HELD,       // may transition to RESERVED or EXPIRED
        RESERVED,   // may not transition to any other state
        EXPIRED,    // may not transition to any other state
    }

    /** The seatHold identifier, unique for this particular performance. */
    private final int seatHoldId;

    /** The customer's email address. */
    private final String customerEmail;

    /** The list of held seats. Order is unspecified. */
    private final List<Seat> seats;

    /** The state of this seatHold. */
    private SeatHold.State state = State.HELD;

    /**
     * Constructs a new SeatHold.
     *
     * @param seatHoldId this seatHold's ID
     * @param customerEmail the customer's email address
     * @param seats the held seats
     */
    SeatHold(int seatHoldId, String customerEmail, List<Seat> seats) {
        if (!isValidEmailAddress(customerEmail)) {
            throw new IllegalArgumentException("invalid email address: " + customerEmail);
        }
        this.seatHoldId = seatHoldId;
        this.customerEmail = customerEmail;
        this.seats = seats;
    }

    /**
     * The seatHold ID.
     *
     * @return the seatHold ID
     */
    public int getSeatHoldId() {
        return seatHoldId;
    }

    /**
     * The customer email address.
     *
     * @return the customer email address
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * The held seats. Note that a pointer to the list contained herein is returned,
     * and this list is NOT immutable. The caller can add or remove seats.
     *
     * @return the held seats
     */
    public List<Seat> getHeldSeats() {
        return seats;
    }

    /**
     * The held state of the seatHold.
     *
     * @return true if the state of the seatHold is HELD, otherwise false
     */
    public boolean isHeld() {
        return state == State.HELD;
    }

    /**
     * The reserved state of the seatHold.
     *
     * @return true if the state of the seatHold is RESERVED, otherwise false
     */
    public boolean isReserved() {
        return state == State.RESERVED;
    }

    /**
     * The expired state of the seatHold.
     *
     * @return true if the state of the seatHold is EXPIRED, otherwise false
     */
    public boolean isExpired() {
        return state == State.EXPIRED;
    }

    /**
     * Expires the seatHold. This method may be invoked more than once without trauma.
     * If the current state is EXPIRED or RESERVED then nothing happens -- it remains
     * in that state.
     *
     * @return true if the seatHold is now EXPIRED, otherwise false
     */
    boolean expire() {
        if (state == State.HELD) {
            state = State.EXPIRED;
        }
        return state == State.EXPIRED;
    }

    /**
     * Reserves the seatHold. This method may be invoked more than once without trauma.
     * If the current state is EXPIRED or RESERVED then nothing happens -- it remains
     * in that state.
     *
     * @return true if the seatHold is now RESERVED, otherwise false
     */
    boolean reserve() {
        if (state == State.HELD) {
            state = State.RESERVED;
        }
        return state == State.RESERVED;
    }

    /**
     * Checks whether the specified email address is valid.
     *
     * @param customerEmail the email address
     * @return true if the email address is valid, otherwise false
     */
    @VisibleForTesting
    static boolean isValidEmailAddress(String customerEmail) {
        if (customerEmail == null) {
            return false;
        }
        // this is overly simplistic
        return customerEmail.matches("^[.a-zA-Z0-9]+@[a-zA-Z0-9]+(.[a-zA-Z]{2,})$");
    }
}
