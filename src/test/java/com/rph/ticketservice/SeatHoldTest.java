package com.rph.ticketservice;

import org.junit.Test;

import static com.rph.ticketservice.SeatHold.isValidEmailAddress;
import static org.junit.Assert.*;

public class SeatHoldTest {

    @Test
    public void testConstructor() {
    }

    @Test
    public void testIsValidEmailAddress() {
        assertFalse(isValidEmailAddress(""));
        assertFalse(isValidEmailAddress("ronald.hughesATgmail.com"));
        assertTrue(isValidEmailAddress("ronald.hughes@gmail.com"));
    }
}
