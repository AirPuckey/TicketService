package com.rph.ticketservice.implementation;

import org.junit.Test;

import static org.junit.Assert.*;


public class SeatGridTest {

    @Test
    public void testSettersAndGetters() {
        SeatGrid seatGrid = new SeatGrid(new Venue(10, 20, 5));
        assertTrue(seatGrid.isAvailable(5, 15));
        seatGrid.setAvailability(5, 15, false);
        assertFalse(seatGrid.isAvailable(5, 15));
        Seat seat = seatGrid.getSeat(5, 15);
        assertEquals(5, seat.getRowNum());
        assertEquals(15, seat.getSeatNumInRow());
        assertEquals(20, seatGrid.getNumSeatsPerRow());
        assertEquals(10, seatGrid.getNumRows());
    }

    @Test
    public void testBuildSeatAvailabilityGrid() {
        boolean[][] seatsAvailabilityGrid = SeatGrid.buildSeatAvailabilityGrid(10, 20);
        assertEquals(10, seatsAvailabilityGrid.length);
        assertEquals(20, seatsAvailabilityGrid[0].length);
        assertTrue(seatsAvailabilityGrid[5][15]);
        seatsAvailabilityGrid[5][15] = false;
        assertFalse(seatsAvailabilityGrid[5][15]);
        seatsAvailabilityGrid[5][15] = true;
        assertTrue(seatsAvailabilityGrid[5][15]);
    }
}
