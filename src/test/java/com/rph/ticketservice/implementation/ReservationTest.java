package com.rph.ticketservice.implementation;

import com.rph.ticketservice.implementation.Reservation;
import com.rph.ticketservice.implementation.Seat;
import com.rph.ticketservice.implementation.SeatHold;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReservationTest {

    @Test
    public void testAll() {
        List<Seat> heldSeats = new LinkedList<>();
        heldSeats.add(new Seat(4, 9, 99));
        SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        Reservation reservation = new Reservation(seatHold, "17");
        assertEquals("17", reservation.getReservationId());
        assertTrue(seatHold == reservation.getSeatHold());
    }
}
