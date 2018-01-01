package com.rph.ticketservice.implementation;

import com.rph.ticketservice.implementation.Seat;
import com.rph.ticketservice.implementation.SeatHold;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.rph.ticketservice.implementation.SeatHold.isValidEmailAddress;
import static org.junit.Assert.*;

public class SeatHoldTest {

    @Test
    public void testToString() {
        List<Seat> heldSeats = new LinkedList<>();
        heldSeats.add(new Seat(4, 9, 99));
        SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertEquals("HELD 17: 1 5x10:99", seatHold.toString());
    }

    @Test
    public void testIsValidEmailAddress() {
        assertFalse(isValidEmailAddress(null));
        assertFalse(isValidEmailAddress(""));
        assertFalse(isValidEmailAddress("ronald.hughesATgmail.com"));
        assertTrue(isValidEmailAddress("ronald.hughes@gmail.com"));
    }

    @Test
    public void testReserveSeats_IsReserved_IsHeld() {
        List<Seat> heldSeats = new LinkedList<>();
        heldSeats.add(new Seat(4, 9, 99));
        SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(seatHold.isHeld());
        assertFalse(seatHold.isReserved());
        assertEquals(1, heldSeats.size());
        assertEquals(1, heldSeats.size());

        seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(seatHold.isHeld());
        assertEquals(1, heldSeats.size());
        seatHold.expire();
        assertEquals(1, heldSeats.size());
        assertFalse(seatHold.isHeld());
        assertFalse(seatHold.reserve());
        assertTrue(seatHold.isExpired());
        assertEquals(1, heldSeats.size());
    }

    @Test
    public void testExpire_IsExpired() {
        List<Seat> heldSeats = new LinkedList<>();
        heldSeats.add(new Seat(4, 9, 99));
        SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(seatHold.isHeld());
        assertFalse(seatHold.isExpired());
        assertEquals(1, heldSeats.size());
        seatHold.expire();
        assertTrue(seatHold.isExpired());
        assertEquals(1, heldSeats.size());
    }

    @Test
    public void testGetters() {
        List<Seat> heldSeats = new LinkedList<>();
        heldSeats.add(new Seat(4, 9, 99));
        SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(heldSeats == seatHold.getHeldSeats());
        assertEquals("ronald.hughes@gmail.com", seatHold.getCustomerEmail());
        assertEquals(17, seatHold.getSeatHoldId());
    }

    @Test
    public void testConstructor() {
        List<Seat> heldSeats = new LinkedList<>();
        heldSeats.add(new Seat(4, 9, 99));
        SeatHold seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(heldSeats == seatHold.getHeldSeats());
        assertEquals("ronald.hughes@gmail.com", seatHold.getCustomerEmail());
        assertEquals(17, seatHold.getSeatHoldId());

        try {
            seatHold = new SeatHold(17, "foo", heldSeats);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
    }
}
