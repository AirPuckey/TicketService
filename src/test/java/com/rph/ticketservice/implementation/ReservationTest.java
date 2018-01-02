package com.rph.ticketservice.implementation;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReservationTest {

    @Test
    public void testAll() {
        List<SeatImpl> heldSeats = new LinkedList<>();
        heldSeats.add(new SeatImpl(4, 9, 99));
        SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
        Reservation reservation = new Reservation(seatHold, "17");
        assertEquals("17", reservation.getReservationId());
        assertTrue(seatHold == reservation.getSeatHold());
    }
}
