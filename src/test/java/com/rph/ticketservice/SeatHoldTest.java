package com.rph.ticketservice;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.rph.ticketservice.SeatHold.isValidEmailAddress;
import static org.junit.Assert.*;

public class SeatHoldTest {

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
        try {
            seatHold.reserveSeats();
        } catch (HoldExpiredException e) {
            throw new RuntimeException(e);
        }
        assertTrue(seatHold.isReserved());
        assertFalse(seatHold.isHeld());
        assertEquals(1, heldSeats.size());

        seatHold = new SeatHold(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(seatHold.isHeld());
        assertEquals(1, heldSeats.size());
        seatHold.expire();
        assertEquals(1, heldSeats.size());
        try {
            seatHold.reserveSeats();
            fail("Exception expected!");
        } catch (HoldExpiredException e) {
            // expected exception
        }
        assertTrue(seatHold.isExpired());
        assertFalse(seatHold.isHeld());
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
