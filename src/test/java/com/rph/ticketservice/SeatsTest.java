package com.rph.ticketservice;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class SeatsTest {

    @Test
    public void testSettersAndGetters() {
        Seats seats = new Seats(new Venue(10, 20, 4));
        assertTrue(seats.isAvailable(5, 15));
        seats.setAvailability(5, 15, false);
        assertFalse(seats.isAvailable(5, 15));
        Seat seat = seats.getSeat(5, 15);
        assertEquals(5, seat.getRowNum());
        assertEquals(15, seat.getSeatNumInRow());
        assertEquals(20, seats.getNumSeatsPerRow());
        assertEquals(10, seats.getNumRows());
    }

    @Test
    public void testBuildSeatAvailabilityGrid() {
        boolean[][] seatsAvailabilityGrid = Seats.buildSeatAvailabilityGrid(10, 20);
        assertEquals(10, seatsAvailabilityGrid.length);
        assertEquals(20, seatsAvailabilityGrid[0].length);
        assertTrue(seatsAvailabilityGrid[5][15]);
        seatsAvailabilityGrid[5][15] = false;
        assertFalse(seatsAvailabilityGrid[5][15]);
        seatsAvailabilityGrid[5][15] = true;
        assertTrue(seatsAvailabilityGrid[5][15]);
    }
}
