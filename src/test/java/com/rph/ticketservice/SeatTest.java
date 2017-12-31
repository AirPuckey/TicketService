package com.rph.ticketservice;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeatTest {

    @Test
    public void testAll() {
        Seat seat = new Seat(5, 15, 77);
        assertEquals("5x15:77", seat.toString());
        assertEquals(5258317, seat.hashCode());
        assertFalse(seat.equals(null));
        assertTrue(seat.equals(seat));
        assertFalse(seat.equals(new Object()));
        Seat equalSeat = new Seat(5, 15, 77);
        assertTrue(seat.equals(equalSeat));
        assertTrue(equalSeat.equals(seat));
        Seat differentSeat = new Seat(7, 11, 21);
        assertFalse(seat.equals(differentSeat));
        assertFalse(differentSeat.equals(seat));
        assertEquals(77, seat.getBestness());
        assertEquals(5, seat.getRowNum());
        assertEquals(15, seat.getSeatNumInRow());
    }
}
