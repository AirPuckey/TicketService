package com.rph.ticketservice.implementation;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.rph.ticketservice.implementation.SeatHoldImpl.isValidEmailAddress;
import static org.junit.Assert.*;

public class SeatHoldTest {

    @Test
    public void testToString() {
        List<SeatImpl> heldSeats = new LinkedList<>();
        heldSeats.add(new SeatImpl(4, 9, 99));
        SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
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
    public void testReserve_IsReserved_IsHeld() {
        List<SeatImpl> heldSeats = new LinkedList<>();
        heldSeats.add(new SeatImpl(4, 9, 99));
        SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(seatHold.isHeld());
        assertFalse(seatHold.isReserved());
        assertFalse(seatHold.isExpired());
        assertEquals(1, heldSeats.size());
        boolean isReserved = seatHold.reserve();
        assertTrue(isReserved);
        assertFalse(seatHold.isHeld());
        assertTrue(seatHold.isReserved());
        assertFalse(seatHold.isExpired());
        assertEquals(1, heldSeats.size());

        seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
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
        List<SeatImpl> heldSeats = new LinkedList<>();
        heldSeats.add(new SeatImpl(4, 9, 99));
        SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(seatHold.isHeld());
        assertFalse(seatHold.isExpired());
        assertEquals(1, heldSeats.size());
        seatHold.expire();
        assertTrue(seatHold.isExpired());
        assertEquals(1, heldSeats.size());
    }

    @Test
    public void testGetters() {
        List<SeatImpl> heldSeats = new LinkedList<>();
        SeatImpl seat = new SeatImpl(4, 9, 99);
        heldSeats.add(seat);
        SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(heldSeats == seatHold.getHeldSeats());
        assertEquals("ronald.hughes@gmail.com", seatHold.getCustomerEmail());
        assertEquals(17, seatHold.getSeatHoldId());
        assertEquals(1, seatHold.numSeatsHeld());
        assertEquals(seat, seatHold.getSeat(0));
    }

    @Test
    public void testConstructor() {
        List<SeatImpl> heldSeats = new LinkedList<>();
        heldSeats.add(new SeatImpl(4, 9, 99));
        SeatHoldImpl seatHold = new SeatHoldImpl(17, "ronald.hughes@gmail.com", heldSeats);
        assertTrue(heldSeats == seatHold.getHeldSeats());
        assertEquals("ronald.hughes@gmail.com", seatHold.getCustomerEmail());
        assertEquals(17, seatHold.getSeatHoldId());

        try {
            seatHold = new SeatHoldImpl(17, "foo", heldSeats);
            fail("Exception expected!");
        } catch (IllegalArgumentException e) {
            // expected exception
        }
    }
}
