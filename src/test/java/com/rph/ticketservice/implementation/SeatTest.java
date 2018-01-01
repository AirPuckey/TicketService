package com.rph.ticketservice.implementation;

import com.rph.ticketservice.implementation.Seat;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SeatTest {

    @Test
    public void testAll() {
        Seat seat = new Seat(5, 15, 77);
        assertEquals("6x16:77", seat.toString());
        assertEquals(20554317, seat.hashCode());
        seat = new Seat(9, 19, 199);
        assertEquals(199, seat.getBestness());
        assertEquals(9, seat.getRowNum());
        assertEquals(19, seat.getSeatNumInRow());
        assertEquals("10x20:199", seat.toString());
        assertEquals(36870855, seat.hashCode());
        assertFalse(seat.equals(null));
        assertTrue(seat.equals(seat));
        assertFalse(seat.equals(new Object()));
        Seat equalSeat = new Seat(9, 19, 199);
        assertTrue(seat.equals(equalSeat));
        assertTrue(equalSeat.equals(seat));
        Seat differentSeat = new Seat(7, 11, 21);
        assertFalse(seat.equals(differentSeat));
        assertFalse(differentSeat.equals(seat));
    }
}
